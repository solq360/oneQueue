package com.eyu.onequeue.store.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.util.FileUtil;
import com.eyu.onequeue.util.NumRecordUtil;
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
    private LinkedHashMap<Long, FileOperate> removeRecords = new LinkedHashMap<>();

    ////////////////// 消息队列目的是 减少传输时数据量
    ////////////////// 本来传输过程是zip压缩的，不用省硬盘空间//////////////////////
    @JsonIgnore
    private Object persistQueueLock = new Object();
    @JsonIgnore
    private List<Object> persistQueue;

    // 定时把消息队形写进文件底层，并做文件资源关闭
    public static FileIndexer of(String topic) {
	FileIndexer ret = null;
	final String rootPath = QMConfig.getInstance().getStoreRootPath(topic);
	String thisFileName = rootPath + FileIndexer.class.getSimpleName();
	if (new File(thisFileName).exists()) {
	    ret = SerialUtil.readValueAsFile(thisFileName, FileIndexer.class);
	} else {
	    ret = new FileIndexer();
	    ret.topic = topic;
	}
	ret.persistQueue = new ArrayList<>(QMConfig.getInstance().STORE_QUEUE_BUFFER_SIZE);
	return ret;
    }

    public void close() {
	wirteToBuffer();
	foreachAndLockFileOperate(fileOperate -> FileUtil.close(fileOperate));
	persistFileIndexer();
    }

    public void clean() {
	Set<Long> removes = new HashSet<>();
	foreachAndLockFileOperate(fileOperate -> {
	    fileOperate.persist();
	    final long checkTime = System.currentTimeMillis() - fileOperate.getLastOperate();
	    if (checkTime > QMConfig.getInstance().STORE_FILE_CLOSE_INTERVAL) {
		FileUtil.close(fileOperate);
	    }
	    if (checkTime > QMConfig.getInstance().STORE_FILE_DELETE_INTERVAL) {
		removes.add(fileOperate.getFileNum());
	    }
	});

	if (!removes.isEmpty()) {
	    synchronized (this) {
		for (long key : removes) {
		    FileOperate fileOperate = indexers.remove(key);
		    if (fileOperate == null) {
			continue;
		    }
		    removeRecords.put(key, fileOperate);
		}
	    }
	}
	if (NumRecordUtil.STORE_MEMORY.getValue() < QMConfig.getInstance().STORE_MEMORY_MAX_SIZE) {
	    return;
	}
	foreachAndLockFileOperate(fileOperate -> {
	    fileOperate.persist();
	    final long checkTime = System.currentTimeMillis() - fileOperate.getLastOperate();
	    if (checkTime > QMConfig.getInstance().STORE_MEMORY_GUARD_CLOSE_INTERVAL) {
		FileUtil.close(fileOperate);
	    }
	});
    }

    public void persist() {
	wirteToBuffer();
	clean();
	persistFileIndexer();
    }

    public void write(byte[]... values) {
	int len = 0;
	for (byte[] bytes : values) {
	    len += bytes.length;
	}
	FileOperate fileOperate = findLastFileIndexer(len);
	synchronized (fileOperate) {
	    fileOperate.wirte(values);
	}
    }

    public void write(Object... messages) {
	synchronized (persistQueueLock) {
	    // 超过队列边界大小，切换操作
	    if (persistQueue.size() + messages.length > QMConfig.getInstance().STORE_QUEUE_PERSIST_SIZE * QMConfig.getInstance().STORE_QUEUE_BUFFER_SIZE) {
		wirteToBuffer();
	    }
	    Collections.addAll(persistQueue, messages);
	}
    }

    public QConsume query(QQuery query) {
	List<Object> list = new LinkedList<>();
	AtomicLong offset = new AtomicLong(query.getStartOffset());
	AtomicLong retAddSize = new AtomicLong();

	foreachAndLockFileOperate(fileOperate -> {
	    // 已读数据过滤
	    if (offset.get() > fileOperate.toAbsoluteOffset()) {
		return;
	    }
	    // 防查询数据过大
	    if (retAddSize.get() >= QMConfig.getInstance().STORE_QUEUE_MAX_SIZE) {
		return;
	    }

	    int queryStart = fileOperate.readyReadNext(offset.get());
	    while (true) {
		byte[] data = null;
		try {
		    data = fileOperate.next();
		} catch (Exception e) {
		    e.printStackTrace();
		    break;
		}
		if (data == null) {
		    break;
		}

		list.addAll(SerialUtil.readList(SerialUtil.unZip(data), Map.class));
		int addCount = data.length + 4;
		queryStart += addCount;
		retAddSize.addAndGet(addCount);

		data = null;
		// 防查询数据过大
		if (retAddSize.get() >= QMConfig.getInstance().STORE_QUEUE_MAX_SIZE) {
		    break;
		}
	    }
	    queryStart += fileOperate.getFiexdOffset();
	    if (queryStart > offset.get()) {
		offset.set(queryStart);
	    }
	});
	return QConsume.of(topic, offset.get(), list.toArray(new Object[list.size()]));
    }

    public QConsume queryForRaw(QQuery query) {
	List<byte[]> list = new LinkedList<>();
	AtomicLong offset = new AtomicLong(query.getStartOffset());
	AtomicLong retAddSize = new AtomicLong();

	foreachAndLockFileOperate(fileOperate -> {
	    // 已读数据过滤
	    if (offset.get() > fileOperate.toAbsoluteOffset()) {
		return;
	    }
	    // 防查询数据过大
	    if (retAddSize.get() >= QMConfig.getInstance().STORE_QUEUE_MAX_SIZE) {
		return;
	    }
	    int queryStart = fileOperate.readyReadNext(offset.get());
	    while (true) {
		byte[] data = null;
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
		queryStart += addCount;
		retAddSize.addAndGet(addCount);

		data = null;
		// 防查询数据过大
		if (retAddSize.get() >= QMConfig.getInstance().STORE_QUEUE_MAX_SIZE) {
		    break;
		}
	    }
	    queryStart += fileOperate.getFiexdOffset();

	    if (queryStart > offset.get()) {
		offset.set(queryStart);
	    }
	});

	int len = 0;
	for (byte[] data : list) {
	    len += PacketUtil.getAutoWriteLen(data.length) + data.length;
	}
	byte[] rawData = new byte[len];
	int os = 0;
	for (byte[] data : list) {
	    int autoLen = PacketUtil.autoWriteNum(os, data.length, rawData);
	    PacketUtil.writeBytes(os += autoLen, data, rawData);
	    os += data.length;
	}
	list.clear();

	return QConsume.ofRaw(topic, offset.get(), rawData);
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
	String thisFileName = QMConfig.getInstance().getStoreRootPath(topic) + FileIndexer.class.getSimpleName();
	SerialUtil.writeValueAsFile(thisFileName, this);
    }

    @SuppressWarnings("resource")
    private synchronized FileOperate findLastFileIndexer(int addSzie) {
	FileOperate ret = indexers.get(ai.get());
	while (ret == null || (ret.getSize() + addSzie) >= QMConfig.getInstance().STORE_SPLIT_SIZE) {
	    long lastFileIndexer = ai.incrementAndGet();
	    if (ret != null) {
		offset = ret.toAbsoluteOffset();
	    }
	    ret = indexers.get(lastFileIndexer);
	    if (ret == null) {
		ret = FileOperate.of(topic, offset, lastFileIndexer);
		indexers.put(lastFileIndexer, ret);
	    }
	}
	offset = ret.toAbsoluteOffset();
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

    public synchronized LinkedHashMap<Long, FileOperate> getRemoveRecords() {
	return new LinkedHashMap<>(this.removeRecords);
    }

}
