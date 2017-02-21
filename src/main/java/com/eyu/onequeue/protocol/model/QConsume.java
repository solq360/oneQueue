package com.eyu.onequeue.protocol.model;

import java.util.Map;
import java.util.function.Consumer;

import com.eyu.onequeue.protocol.anno.QOpCode;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 临时对象，负责 业务与qpacket数据交互
 * 
 * @author solq
 */
@QOpCode(QOpCode.QCONSUME)
@JsonInclude(Include.NON_EMPTY)
public class QConsume implements IRecycle {
    /**
     * 最后读取指针记录
     */
    private long o;
    /**
     * 数据
     */
    private Object[] b;
    /**
     * topic
     */
    private String t;
    /**
     * raw数据
     */
    private byte[] r;

    public byte[] toBytes() {
	return SerialUtil.writeValueAsBytes(this);
    }

    public static QConsume byte2Object(byte[] bytes) {
	return SerialUtil.readValue(bytes, QConsume.class);
    }

    public void foreachMessageData(Consumer<Object[]> action) {
	if (b == null && r != null) {
	    int os = 0;
	    while (os < r.length) {
		int len = 0;
		try {
		    len = PacketUtil.autoReadNum(os, r).intValue();
		    byte[] tBytes = PacketUtil.readBytes(os += r[os], len, r);
		    Object[] t = SerialUtil.readArray(SerialUtil.unZip(tBytes), Map.class);
		    action.accept(t);
		    tBytes = null;
		} catch (Exception e) {
		    e.printStackTrace();
		}
		if (len == 0) {
		    break;
		}
		os += len;
	    }
	} else {
	    action.accept(b);
	}

    }

    // getter

    public long getO() {
	return o;
    }

    public String getT() {
	return t;
    }

    public Object[] getB() {

	return b;
    }

    public byte[] getR() {
	return r;
    }

    public static QConsume ofRaw(String topic, long offset, byte[] rawData) {
	QConsume ret = new QConsume();
	ret.t = topic;
	ret.o = offset;
	ret.r = rawData;
	return ret;
    }

    public static QConsume of(String topic, long offset, Object[] body) {
	QConsume ret = new QConsume();
	ret.t = topic;
	ret.o = offset;
	ret.b = body;
	return ret;
    }

    @Override
    public void recycle() {
	b = null;
	t = null;
    }

    public QProduce toProduce() {
	return QProduce.of(t, b);
    }

}
