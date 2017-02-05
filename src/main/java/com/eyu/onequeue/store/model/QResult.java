package com.eyu.onequeue.store.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.eyu.onequeue.protocol.anno.QModel;
import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

/**
 * @author solq
 */
@QModel(QModel.QRESULT)
public class QResult {
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
    private String topic;

    public byte[] toBytes() {

	byte[] topicBytes = topic.getBytes();
	final int len = Integer.BYTES + topicBytes.length + Integer.BYTES + rawData.length + Long.BYTES;
	byte[] ret = new byte[len];
	PacketUtil.writeInt(0, topicBytes.length, ret);
	PacketUtil.writeBytes(Integer.BYTES, topicBytes, ret);
	PacketUtil.writeInt(Integer.BYTES + topicBytes.length, rawData.length, ret);
	PacketUtil.writeBytes(Integer.BYTES + topicBytes.length + Integer.BYTES, rawData, ret);
	PacketUtil.writeLong(Integer.BYTES + topicBytes.length + Integer.BYTES + rawData.length, offset, ret);

	return ret;
    }

    public static QResult byte2Object(byte[] bytes) {

	final int tLen = PacketUtil.readInt(0, bytes);
	final String topic = PacketUtil.readString(Integer.BYTES, tLen, bytes);
	final int dataLen = PacketUtil.readInt(Integer.BYTES + tLen, bytes);
	byte[] rawData = PacketUtil.readBytes(Integer.BYTES + tLen + Integer.BYTES, dataLen, bytes);
	final long offset = PacketUtil.readLong(Integer.BYTES + tLen + Integer.BYTES + rawData.length, bytes);
	return ofRaw(topic, offset, rawData);
    }

    public void foreachMessageData(Consumer<QMessage<?, ?>[]> action) {
	rawToMessageData(action);
    }

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
	return topic;
    }

    public byte[] getRawData() {
	return rawData;
    }

    public static QResult ofRaw(String topic, long offset, byte[] rawData) {
	QResult ret = new QResult();
	ret.topic = topic;
	ret.offset = offset;
	ret.rawData = rawData;
	return ret;
    }

}
