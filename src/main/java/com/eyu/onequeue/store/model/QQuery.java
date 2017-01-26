package com.eyu.onequeue.store.model;

public class QQuery {
    private String topic;

    private long startOffset;
    private Long endOffset;

    public long getStartOffset() {
	return startOffset;
    }

    public Long getEndOffset() {
	return endOffset;
    }

    public String getTopic() {
	return topic;
    }

    public static QQuery of(String topic,int startOffset) {
	QQuery ret = new QQuery();
	ret.topic = topic;
	ret.startOffset = startOffset;
	return ret;
    }

}
