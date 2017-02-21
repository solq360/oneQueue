package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

import com.eyu.onequeue.util.SerialUtil;

/***
 * 
 * @author solq
 */
public class QMapParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = SerialUtil.writeValueAsBytes(obj);
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return SerialUtil.readValue(bytes, type);
    }

    @Override
    public byte getType() {
	return TypeCode.MAP;
    }

}
