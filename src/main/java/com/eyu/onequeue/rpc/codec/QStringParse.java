package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 
 * @author solq
 */
public class QStringParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = PacketUtil.getStringBytes((String) obj);
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return PacketUtil.byteToString(bytes);
    }

    @Override
    public byte getType() {
	return TypeCode.STRING;
    }

}
