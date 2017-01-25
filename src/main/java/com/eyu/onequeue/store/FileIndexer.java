package com.eyu.onequeue.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.eyu.onequeue.MQServerConfig;
import com.eyu.onequeue.store.model.FileOperate;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;
import com.eyu.onequeue.util.FileUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 文件物理与业务逻辑层处理 <br>
 * 文件索引 维护所有 FileOperate 并做数据存储操作，支持并发 ,支持缓冲消息
 * 
 * @author solq
 */
public class FileIndexer {
    private String topic;

    private long offset;

    private AtomicLong ai = new AtomicLong();

    private LinkedHashMap<Long, FileOperate> indexers = new LinkedHashMap<>();

    private long lastFileIndexer = 0;

    ////////////////// 消息队列目的是 减少传输时数据量//////////////////////
    @JsonIgnore
    private Object persistQueueLock = new Object();
    @JsonIgnore
    private List<Object> persistQueue;

    // 定时把消息队形写进文件底层，并做文件资源关闭
    @JsonIgnore
    private Thread monitor = new Thread(new Runnable() {

	private void doTask() {
	    wirteToBuffer();
	    long now = System.currentTimeMillis();
	    foreachAndLockFileOperate(fileOperate -> {
		fileOperate.persist();
		if ((now - fileOperate.getLastOperate()) < MQServerConfig.STORE_FILE_CLOSE_INTERVAL) {
		    return;
		}
		FileUtil.close(fileOperate);
	    });

	    persistFileIndexer();
	}

	@Override
	public void run() {
	    while (!Thread.interrupted()) {
		doTask();
		try {
		    Thread.sleep(MQServerConfig.STORE_FILE_PERSIST_INTERVAL);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	    close();
	}
    }, " qm message persist monitor : " + topic);

    public static FileIndexer of(String topic) {
	FileIndexer ret = null;
	final String rootPath = MQServerConfig.getStoreRootPath(topic);
	String thisFileName = rootPath + FileIndexer.class.getSimpleName();
	if (new File(thisFileName).exists()) {
	    ret = SerialUtil.readValueAsFile(thisFileName, FileIndexer.class);
	} else {
	    ret = new FileIndexer();
	    ret.topic = topic;
	}
	if (MQServerConfig.STORE_QUEUE_OPEN) {
	    ret.persistQueue = new ArrayList<>(MQServerConfig.STORE_QUEUE_BUFFER_SIZE);
	}

	ret.monitor.setDaemon(true);
	ret.monitor.start();
	return ret;
    }

    public void close() {
	persist();
	foreachAndLockFileOperate(fileOperate -> FileUtil.close(fileOperate));
	persistFileIndexer();
    }

    public void persist() {
	wirteToBuffer();
	foreachAndLockFileOperate(fileOperate -> fileOperate.persist());
	persistFileIndexer();
    }

    public void write(byte[] bytes) {
	FileOperate fileOperate = findLastFileIndexer(bytes.length);
	synchronized (fileOperate) {
	    fileOperate.wirte(bytes);
	}
    }

    public void write(Object... messages) {
	boolean wirteQueueSucceed = false;
	if (MQServerConfig.STORE_QUEUE_OPEN) {
	    synchronized (persistQueueLock) {
		// 超过队列边界大小，切换操作
		if (persistQueue.size() + messages.length > MQServerConfig.STORE_QUEUE_PERSIST_SIZE) {
		    wirteToBuffer();
		    wirteQueueSucceed = false;
		} else {
		    Collections.addAll(persistQueue, messages);
		    wirteQueueSucceed = true;
		}
	    }
	}
	// 没把消息加入队列，直接写入底层
	if (!wirteQueueSucceed) {
	    write(SerialUtil.writeValueAsZipBytes(messages));
	}

    }

    public QResult query(QQuery query) {
	List<byte[]> list = new LinkedList<>();
	AtomicLong offset = new AtomicLong();
	final long start = query.getStartOffset();
	foreachAndLockFileOperate(fileOperate -> {
	    if (start > fileOperate.toOffset()) {
		return;
	    }
	    fileOperate.readyReadNext();
	    byte[] data = null;
	    do {
		try {
		    data = fileOperate.next();
		    list.add(data);
		} catch (Exception e) {
		    e.printStackTrace();
		    break;
		}
	    } while (data != null);

	    long t = Math.max(offset.get(), fileOperate.toOffset());
	    offset.set(t);
	});
	return QResult.of(offset.get(), list);
    }
    //////////////////////////////////////////////////////////////////////

    private void foreachAndLockFileOperate(Consumer<FileOperate> action) {
	Map<Long, FileOperate> indexers = getIndexers();
	for (Entry<Long, FileOperate> r : indexers.entrySet()) {
	    final FileOperate fileOperate = r.getValue();
	    synchronized (fileOperate) {
		try {
		    action.accept(fileOperate);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    private void wirteToBuffer() {
	if (!MQServerConfig.STORE_QUEUE_OPEN) {
	    return;
	}
	byte[] bytes = null;
	synchronized (persistQueueLock) {
	    if (persistQueue.isEmpty()) {
		return;
	    }
	    bytes = SerialUtil.writeValueAsZipBytes(persistQueue);
	    persistQueue.clear();
	}
	if (bytes != null) {
	    write(bytes);
	}
    }

    private synchronized void persistFileIndexer() {
	String thisFileName = MQServerConfig.getStoreRootPath(topic) + FileIndexer.class.getSimpleName();
	SerialUtil.writeValueAsFile(thisFileName, this);
    }

    @SuppressWarnings("resource")
    private synchronized FileOperate findLastFileIndexer(int addSzie) {
	FileOperate ret = indexers.get(lastFileIndexer);
	while (ret == null || (ret.getSize() + addSzie) >= MQServerConfig.STORE_SPLIT_SIZE) {
	    lastFileIndexer = ai.getAndIncrement();
	    ret = indexers.get(lastFileIndexer);
	    if (ret == null) {
		ret = FileOperate.of(topic, offset, lastFileIndexer);
		indexers.put(lastFileIndexer, ret);
	    }
	}
	return ret;
    }

    // getter

    public String getTopic() {
	return topic;
    }

    public long getOffset() {
	return offset;
    }

    public AtomicLong getAi() {
	return ai;
    }

    public synchronized LinkedHashMap<Long, FileOperate> getIndexers() {
	return new LinkedHashMap<>(this.indexers);
    }

    public long getLastFileIndexer() {
	return lastFileIndexer;
    }

}
