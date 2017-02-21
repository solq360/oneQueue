package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

/***
 * 
 * @author solq
 */
public class QBytesParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = (byte[]) obj;
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return bytes;
    }

    @Override
    public byte getType() {
	return TypeCode.BYTES;
    }

}
