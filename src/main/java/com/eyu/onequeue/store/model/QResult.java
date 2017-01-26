package com.eyu.onequeue.store.model;

import java.util.List;

import com.eyu.onequeue.protocol.model.QPacket;

/**
 * @author solq
 */
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
     * topic
     */
    private String topic;
    
    
    public QPacket toPacket() {
	QPacket ret = new QPacket();
	return ret;
    }
    
    //getter

    public long getOffset() {
	return offset;
    }

    public List<byte[]> getBatchData() {
	return batchData;
    }

    public String getTopic() {
	return topic;
    }

    public static QResult of(String topic, long offset, List<byte[]> batchData) {
	QResult ret = new QResult();
	ret.topic = topic;
	ret.offset = offset;
	ret.batchData = batchData;
	return ret;
    }



}
