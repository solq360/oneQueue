package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.protocol.anno.QOpCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

/***
 * 属性名采取最少字母命名，减少通信跟存储 生产消息对象
 * 
 * @author solq
 */
@QOpCode(QOpCode.QPRODUCE)
public class QProduce implements IRecycle {
    /** 订阅 **/
    private String t;
    /** 内容信息 **/
    private QMessage<?, ?>[] b;
    /** 作用本地查询 **/
    @JsonIgnore
    private long offset;

    @Override
    public void recycle() {
	b = null;
    }

    // getter
    public long getOffset() {
	return offset;
    }

    public QMessage<?, ?>[] getB() {
	return b;
    }

    public String getT() {
	return t;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public static QProduce of(String topic, QMessage<?, ?>... qmessages) {
	QProduce ret = new QProduce();
	ret.t = topic;
	ret.b = qmessages;
	return ret;
    }

}
