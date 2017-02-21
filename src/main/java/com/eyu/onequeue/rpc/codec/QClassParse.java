package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 
 * @author solq
 */
public class QClassParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = PacketUtil.getStringBytes(((Class<?>) obj).getName());
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return PacketUtil.byteToClass(bytes);
    }

    @Override
    public byte getType() {
	return TypeCode.CLASS;
    }

}
