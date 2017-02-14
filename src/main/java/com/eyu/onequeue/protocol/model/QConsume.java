package com.eyu.onequeue.protocol.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.eyu.onequeue.protocol.anno.QOpCode;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

/**
 * @author solq
 */
@QOpCode(QOpCode.QCONSUME)
public class QConsume implements IRecycle, IByte {
    /**
     * 最后读取指针记录
     */
    private long offset;
    /**
     * 返回raw数据
     */
    private byte[] rawData;
    /**
     * topic
     */
    private byte[] topicBytes;

    /**
     * topic
     */
    private String topic;

    @Override
    public byte[] toBytes() {
	final int len = toSize();
	byte[] ret = new byte[len];
	int offset = 0;
	PacketUtil.writeInt(offset, topicBytes.length, ret);
	PacketUtil.writeBytes(offset += Integer.BYTES, topicBytes, ret);
	PacketUtil.writeInt(offset += topicBytes.length, rawData.length, ret);
	PacketUtil.writeBytes(offset += Integer.BYTES, rawData, ret);
	PacketUtil.writeLong(offset += rawData.length, offset, ret);
	return ret;
    }

    @Override
    public int toSize() {
	return Integer.BYTES + topicBytes.length + Integer.BYTES + rawData.length + Long.BYTES;
    }

    public static QConsume byte2Object(byte[] bytes) {
	final int tLen = PacketUtil.readInt(0, bytes);
	final String topic = PacketUtil.readString(Integer.BYTES, tLen, bytes);
	final int dataLen = PacketUtil.readInt(Integer.BYTES + tLen, bytes);
	byte[] rawData = PacketUtil.readBytes(Integer.BYTES + tLen + Integer.BYTES, dataLen, bytes);
	final long offset = PacketUtil.readLong(Integer.BYTES + tLen + Integer.BYTES + rawData.length, bytes);
	return of(topic, offset, rawData);
    }

    public void foreachMessageData(Consumer<QMessage<?, ?>[]> action) {
	rawToMessageData(action);
    }

    /***
     * 建议使用 foreachMessageData
     * */
    @Deprecated
    public List<QMessage<?, ?>> toMessageData() {
	return rawToMessageData(null);
    }

    List<QMessage<?, ?>> rawToMessageData(Consumer<QMessage<?, ?>[]> action) {
	List<QMessage<?, ?>> ret = null;
	if (action == null) {
	    ret = new LinkedList<>();
	}
	int os = 0;
	while (os < rawData.length) {
	    int len = 0;
	    try {
		len = PacketUtil.readInt(os, rawData);
		byte[] tBytes = PacketUtil.readBytes(os + Integer.BYTES, len, rawData);
		QMessage<?, ?>[] t = SerialUtil.readArray(SerialUtil.unZip(tBytes), QMessage.class);
		if (action == null) {
		    Collections.addAll(ret, t);
		} else {
		    action.accept(t);
		}
		t = null;
		tBytes = null;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    if (len == 0) {
		break;
	    }
	    os += len + Integer.BYTES;
	}
	return ret;
    }

    // getter

    public long getOffset() {
	return offset;
    }

    public String getTopic() {
	if (topic == null) {
	    topic = new String(topicBytes);
	}
	return topic;
    }

    public byte[] getRawData() {
	return rawData;
    }

    public static QConsume of(String topic, long offset, byte[] rawData) {
	QConsume ret = new QConsume();
	ret.topic = topic;
	ret.topicBytes = topic.getBytes();
	ret.offset = offset;
	ret.rawData = rawData;
	return ret;
    }

    @Override
    public void recycle() {
	rawData = null;
	topic = null;
	topicBytes = null;
    }

}
