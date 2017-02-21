package com.eyu.onequeue.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author solq
 **/
public enum NumRecordUtil {
    STORE_MEMORY;

    private AtomicLong value = new AtomicLong();

    public long getValue() {
	return value.get();
    }

    public void add(long value) {
	this.value.addAndGet(value);
    }
}
