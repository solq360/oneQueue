package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 
 * @author solq
 */
public class QBooleanParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = new byte[1];
	PacketUtil.writeBoolean(0, (boolean) obj, ret);
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return PacketUtil.readBoolean(0, bytes);
    }

    @Override
    public byte getType() {
	return TypeCode.BOOLEAN;
    }

}
