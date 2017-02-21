package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 
 * @author solq
 */
public class QShortParse implements IQParse {

    @Override
    public byte[] encode(Object obj) {
	byte[] ret = new byte[2];
	PacketUtil.writeShort(0, (short) obj, ret);
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return PacketUtil.readShort(0, bytes);
    }

    @Override
    public byte getType() {
	return TypeCode.SHORT;
    }

}
