package com.eyu.onequeue.store.model;

public class QQuery {
    private long startOffset;
    private Long endOffset;

    public long getStartOffset() {
	return startOffset;
    }

    public Long getEndOffset() {
	return endOffset;
    }

    public static QQuery of(int startOffset) {
	QQuery ret = new QQuery();
	ret.startOffset = startOffset;
	return ret;
    }

}
