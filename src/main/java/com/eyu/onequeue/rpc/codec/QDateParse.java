package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;
import java.util.Date;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 
 * @author solq
 */
public class QDateParse implements IQParse {
    @Override
    public byte[] encode(Object obj) {
	byte[] ret = new byte[4];
	PacketUtil.writeLong(0, ((Date) obj).getTime(), ret);
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	return new Date(PacketUtil.readLong(0, bytes));
    }

    @Override
    public byte getType() {
	return TypeCode.DATE;
    }

}
