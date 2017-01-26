package com.eyu.onequeue.store;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.eyu.onequeue.MQServerConfig;
import com.eyu.onequeue.util.FileUtil;
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
    // 写入状态记录，持久化优化操作
    private boolean change = false;

    private long lastOperate;

    // jdk mbb 底层
    @JsonIgnore
    private MappedByteBuffer mbb;
    @JsonIgnore
    private FileChannel ch;

    public long toOffset() {
	return fiexdOffset + size;
    }

    public int wirte(byte[] bytes) {
	init();
	final int s = bytes.length;
	if (bytes == null || s == 0) {
	    return size;
	}

	try {
	    mbb.position(size);
	    mbb.putInt(s);
	    mbb.put(bytes, 0, s);
	    size += bytes.length + Integer.BYTES;

	    // record
	    change = true;
	    lastOperate = System.currentTimeMillis();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return size;
    }

    public void readyReadNext() {
	init();
	mbb.position(0);
    }

    public byte[] next() {
	init();
	if (!mbb.hasRemaining()) {
	    return null;
	}
	if (mbb.position() >= size) {
	    return null;
	}

	final int s = mbb.getInt();
	byte[] ret = new byte[s];
	mbb.get(ret);

	// record
	lastOperate = System.currentTimeMillis();

	return ret;
    }

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
    }

    private void init() {
	if (mbb != null) {
	    return;
	}
	RandomAccessFile rf = null;
	try {
	    String fileName = MQServerConfig.getStoreFilePath(topic, fileNum);
	    rf = new RandomAccessFile(fileName, "rw");
	    ch = rf.getChannel();
	    mbb = ch.map(MapMode.READ_WRITE, 0, MQServerConfig.STORE_SPLIT_SIZE);
	    change = false;
	    lastOperate = System.currentTimeMillis();
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

}
