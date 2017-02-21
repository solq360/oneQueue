package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.protocol.anno.QOpCode;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/***
 * 属性名采取最少字母命名，减少通信跟存储 生产消息对象
 * 临时对象，负责 业务与qpacket数据交互
 * @author solq
 */
@QOpCode(QOpCode.QPRODUCE)
public class QProduce implements IRecycle {
    /** 订阅 **/
    private String t;
    /** 内容信息 **/
    private Object[] b;
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

    public void setOffset(long offset) {
	this.offset = offset;
    }

    public String getT() {
	return t;
    }

    public Object[] getB() {
	return b;
    }
 
    public static QProduce of(String topic, Object... qmessages) {
	QProduce ret = new QProduce();
	ret.t = topic;
	ret.b = qmessages;
	return ret;
    }

    public static QProduce of(byte[] bytes) {
 	return SerialUtil.readValue(bytes, QProduce.class);
    }

    public byte[] toBytes() {
	byte[] bytes = SerialUtil.writeValueAsBytes(this);
	return bytes;
    }
}
