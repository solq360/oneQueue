package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

/***
 * 
 * @author solq
 */
public class QByteParse implements IQParse {

    @Override
    public byte[] encode(Object obj) {
	byte[] ret = new byte[1];
	ret[0] = (byte) obj;
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return bytes[0];
    }

    @Override
    public byte getType() {
	return TypeCode.BYTE;
    }

}
