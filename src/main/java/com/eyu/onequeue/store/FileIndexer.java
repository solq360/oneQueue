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

import com.eyu.onequeue.QMServerConfig;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;
import com.eyu.onequeue.util.FileUtil;
import com.eyu.onequeue.util.PacketUtil;
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

    ////////////////// 消息队列目的是 减少传输时数据量//////////////////////
    @JsonIgnore
    private Object persistQueueLock = new Object();
    @JsonIgnore
    private List<Object> persistQueue;

    // 定时把消息队形写进文件底层，并做文件资源关闭

    public static FileIndexer of(String topic) {
	FileIndexer ret = null;
	final String rootPath = QMServerConfig.getStoreRootPath(topic);
	String thisFileName = rootPath + FileIndexer.class.getSimpleName();
	if (new File(thisFileName).exists()) {
	    ret = SerialUtil.readValueAsFile(thisFileName, FileIndexer.class);
	} else {
	    ret = new FileIndexer();
	    ret.topic = topic;
	}
	if (QMServerConfig.STORE_QUEUE_OPEN) {
	    ret.persistQueue = new ArrayList<>(QMServerConfig.STORE_QUEUE_BUFFER_SIZE);
	}
	return ret;
    }

    public void close() {
	wirteToBuffer();
	foreachAndLockFileOperate(fileOperate -> FileUtil.close(fileOperate));
	persistFileIndexer();
    }

    public void persist() {
	wirteToBuffer();
	foreachAndLockFileOperate(fileOperate -> {
	    fileOperate.persist();
	    if ((System.currentTimeMillis() - fileOperate.getLastOperate()) > QMServerConfig.STORE_FILE_CLOSE_INTERVAL) {
		FileUtil.close(fileOperate);
	    }
	});
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
	if (QMServerConfig.STORE_QUEUE_OPEN) {
	    synchronized (persistQueueLock) {
		// 超过队列边界大小，切换操作
		if (persistQueue.size() + messages.length > QMServerConfig.STORE_QUEUE_PERSIST_SIZE) {
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
	AtomicLong retAddSize = new AtomicLong();

	final long start = query.getStartOffset();

	foreachAndLockFileOperate(fileOperate -> {
	    // 已读数据过滤
	    if (start >= fileOperate.toOffset()) {
		return;
	    }
	    // 防查询数据过大
	    if ((offset.get() - start) >= QMServerConfig.STORE_QUEUE_MAX_SIZE) {
		return;
	    }

	    fileOperate.readyReadNext(start);
	    byte[] data = null;
	    long querySize = 0;
	    while (true) {
		try {
		    data = fileOperate.next();
		} catch (Exception e) {
		    e.printStackTrace();
		    break;
		}
		if (data == null) {
		    break;
		}

		list.add(data);
		int addCount = data.length + 4;
		querySize += addCount;
		retAddSize.addAndGet(addCount);

		// 防查询数据过大
		if (retAddSize.get() >= QMServerConfig.STORE_QUEUE_MAX_SIZE) {
		    break;
		}
	    }
	    long t = fileOperate.getFiexdOffset() + querySize;
	    if (t > offset.get()) {
		offset.set(t);
	    }
	});
	
	
	int len = 0;
	for (byte[] data : list) {
	    len += data.length + 4;
	}
	byte[] rawData = new byte[len];
	int os = 0;
	for (byte[] data : list) {
	    PacketUtil.writeInt(os, data.length, rawData);
	    os += 4;
	    PacketUtil.writeBytes(os, data, rawData);
	    os += data.length;
	}
	
	return QResult.ofRaw(topic, offset.get(), rawData);
    }

//    public QResult queryForRaw(QQuery query) {
//	List<byte[]> list = new LinkedList<>();
//	AtomicLong offset = new AtomicLong();
//
//	final long start = query.getStartOffset();
//	foreachAndLockFileOperate(fileOperate -> {
//	    // 已读数据过滤
//	    if (start >= fileOperate.toOffset()) {
//		return;
//	    }
//	    // 防查询数据过大
//	    if ((offset.get() - start) >= QMServerConfig.STORE_QUEUE_MAX_SIZE) {
//		return;
//	    }
//
//	    fileOperate.readyReadNext(start);
//	    byte[] data = null;
//	    try {
//		data = fileOperate.nextAll();
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//	    if (data == null) {
//		return;
//	    }
//
//	    list.add(data);
//	    long t = fileOperate.getFiexdOffset() + data.length;
//	    if (t > offset.get()) {
//		offset.set(t);
//	    }
//	});
//
//	int len = 0;
//	for (byte[] data : list) {
//	    len += data.length + 4;
//	}
//	byte[] rawData = new byte[len];
//	int os = 0;
//	for (byte[] data : list) {
//	    PacketUtil.writeInt(os, data.length, rawData);
//	    os += 4;
//	    PacketUtil.writeBytes(os, data, rawData);
//	    os += data.length;
//	}
//
//	return QResult.ofRaw(topic, offset.get(), rawData);
//    }

    //////////////////////////////////////////////////////////////////////

    private void foreachAndLockFileOperate(Consumer<FileOperate> action) {
	Map<Long, FileOperate> indexers = getIndexers();
	// indexers.values().parallelStream().forEach(action);
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
	if (!QMServerConfig.STORE_QUEUE_OPEN) {
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
	String thisFileName = QMServerConfig.getStoreRootPath(topic) + FileIndexer.class.getSimpleName();
	SerialUtil.writeValueAsFile(thisFileName, this);
    }

    @SuppressWarnings("resource")
    private synchronized FileOperate findLastFileIndexer(int addSzie) {
 	FileOperate ret = indexers.get(ai.get());
	while (ret == null || (ret.getSize() + addSzie) >= QMServerConfig.STORE_SPLIT_SIZE) {
	    long lastFileIndexer = ai.incrementAndGet();
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

}
