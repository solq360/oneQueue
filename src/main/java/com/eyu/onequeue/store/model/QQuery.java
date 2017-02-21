package com.eyu.onequeue.store.model;

/**
 * @author solq
 **/
public class QQuery {
    private String topic;
    private long startOffset;

    
    public long getStartOffset() {
	return startOffset;
    }

    public String getTopic() {
	return topic;
    }

    public static QQuery of(String topic, long startOffset) {
	QQuery ret = new QQuery();
	ret.topic = topic;
	ret.startOffset = startOffset;
	return ret;
    }

}
