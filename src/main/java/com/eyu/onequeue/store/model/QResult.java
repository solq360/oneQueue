package com.eyu.onequeue.store.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * 返回数据
     */
    private List<byte[]> batchData;
    /**
     * 返回数据
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<QMessage<?, ?>> toMessageData() {
	List<QMessage<?, ?>> ret = new LinkedList<>();
	byte[] tBytes = null;
	byte[] stBytes = null;
	int os = 0;
	while (os < rawData.length) {
	    int len = 0;
	    try {
		len = PacketUtil.readInt(os, rawData);
		tBytes = PacketUtil.readBytes(os + Integer.BYTES, len, rawData);

		int sos = 0;
		int slen = 0;
		while (sos < tBytes.length) {
		    slen = PacketUtil.readInt(sos, tBytes);
		    sos += Integer.BYTES;
		    stBytes = SerialUtil.unZip(PacketUtil.readBytes(sos, slen, tBytes));
		    sos += slen;
		    List<QMessage> list = SerialUtil.readArray(stBytes, QMessage.class);
		    ret.addAll((Collection<? extends QMessage<?, ?>>) list);
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

    public List<byte[]> getBatchData() {
	return batchData;
    }

    public String getTopic() {
	return topic;
    }

    public byte[] getRawData() {
	return rawData;
    }

    public static QResult of(String topic, long offset, List<byte[]> batchData) {
	QResult ret = new QResult();
	ret.topic = topic;
	ret.offset = offset;
	ret.batchData = batchData;
	return ret;
    }

    public static QResult ofRaw(String topic, long offset, byte[] rawData) {
	QResult ret = new QResult();
	ret.topic = topic;
	ret.offset = offset;
	ret.rawData = rawData;
	return ret;
    }

}
