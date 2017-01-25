package com.eyu.onequeue.store.model;

import java.util.List;
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

    public long getOffset() {
	return offset;
    }

    public List<byte[]> getBatchData() {
	return batchData;
    }

    public static QResult of(long offset, List<byte[]> batchData) {
	QResult ret = new QResult();
	ret.offset = offset;
	ret.batchData = batchData;
	return ret;
    }

}
