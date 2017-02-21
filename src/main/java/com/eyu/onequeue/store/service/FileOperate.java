package com.eyu.onequeue.store.service;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.util.FileUtil;
import com.eyu.onequeue.util.NumRecordUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * file底层操作包装
 * 
 * @author solq
 */
public class FileOperate implements AutoCloseable {

    private String topic;
    // 文件序列
    private long fileNum;
    // 文件绝对偏移
    private long fiexdOffset;
    // 文件总大小，用来切割同记录最后offset
    private int size;
    // 初始化分配大小 防止以后变更
    private long alloc = QMConfig.getInstance().STORE_SPLIT_SIZE;

    // 写入状态记录，持久化优化操作
    private boolean change = false;
    // 最后读写操作时间
    private long lastOperate = System.currentTimeMillis();

    // jdk mbb 底层
    @JsonIgnore
    private MappedByteBuffer mbb;
    @JsonIgnore
    private FileChannel ch;

    /**
     * 获取绝对偏移
     */
    public long toAbsoluteOffset() {
	return fiexdOffset + size;
    }

    public int wirte(byte[]... values) {
	init();

	for (byte[] bytes : values) {
	    final int s = bytes.length;
	    if (bytes == null || s == 0) {
		continue;
	    }
	    try {
		mbb.position(size);
		mbb.putInt(s);
		mbb.put(bytes);
		size += bytes.length + Integer.BYTES;

		// record
		change = true;
		lastOperate = System.currentTimeMillis();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return size;
    }

    public int readyReadNext(Long offset) {
	init();
	int retOffsetStart = 0;
	Long pos = offset - fiexdOffset;

	if (pos < 0 || pos >= toAbsoluteOffset()) {
	    String msg = ("readyReadNext bug offset : " + offset + "  retOffsetStart :" + retOffsetStart + "  toAbsoluteOffset() :" + toAbsoluteOffset());
	    throw new RuntimeException(msg);
	}
	retOffsetStart = pos.intValue();
	mbb.position(retOffsetStart);
	return retOffsetStart;
    }

    public byte[] next() {
	init();
	if (!mbb.hasRemaining()) {
	    return null;
	}
	if (mbb.position() >= size - 4) {
	    return null;
	}

	final int len = mbb.getInt();
	byte[] ret = new byte[len];
	mbb.get(ret);

	// record
	lastOperate = System.currentTimeMillis();

	return ret;
    }

    // public byte[] nextAll() {
    // init();
    // if (!mbb.hasRemaining()) {
    // return null;
    // }
    // if (mbb.position() >= size) {
    // return null;
    // }
    //
    // final int len = size - mbb.position();
    // byte[] ret = new byte[len];
    // mbb.get(ret);
    //
    // // record
    // lastOperate = System.currentTimeMillis();
    //
    // return ret;
    // }

    public void persist() {
	if (mbb == null) {
	    return;
	}
	if (change && ch.isOpen()) {
	    // ch.force(false);
	    mbb.force();
	    change = false;
	}
    }

    @Override
    public void close() throws Exception {
	persist();
	if (mbb == null) {
	    return;
	}
	if (ch.isOpen()) {
	    ch.close();
	}
	FileUtil.clean(mbb);
	ch = null;
	mbb = null;

	NumRecordUtil.STORE_MEMORY.add(-alloc);
    }

    private void init() {
	if (mbb != null) {
	    return;
	}
	RandomAccessFile rf = null;
	try {
	    String fileName = QMConfig.getInstance().buildStoreFilePath(topic, fileNum);
	    FileUtil.createDirs(fileName);

	    rf = new RandomAccessFile(fileName, "rw");
	    ch = rf.getChannel();
	    mbb = ch.map(MapMode.READ_WRITE, 0, alloc);

	    // record
	    change = false;
	    lastOperate = System.currentTimeMillis();
	    NumRecordUtil.STORE_MEMORY.add(alloc);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    FileUtil.close(rf);
	}
    }

    public static FileOperate of(String topic, long offset, long fileNum) {
	FileOperate ret = new FileOperate();
	ret.topic = topic;
	ret.fiexdOffset = offset;
	ret.fileNum = fileNum;
	return ret;
    }

    // getter

    public String getTopic() {
	return topic;
    }

    public long getFileNum() {
	return fileNum;
    }

    public long getFiexdOffset() {
	return fiexdOffset;
    }

    public int getSize() {
	return size;
    }

    public boolean isChange() {
	return change;
    }

    public long getLastOperate() {
	return lastOperate;
    }

    public long getAlloc() {
	return alloc;
    }

}
