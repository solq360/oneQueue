package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.eyu.onequeue.util.PacketUtil;

/***
 * 动态生成long int 减少字节数
 * @author solq
 */
public class QNumParse implements IQParse {

    private static class NumOpcode {
	public final static byte ZERO = 0;
	public final static byte BYTE = 1;
	public final static byte SHORT = 2;
	public static final byte INT = 3;
	public static final byte LONG = 4;
	public static final byte DOUBLE = 5;
	public static final byte FLOAT = 6;
    }

    @Override
    public byte[] encode(Object obj) {
	int offset = 1;
	byte[] ret = null;
	byte opcode = -1;
	Number num = (Number) obj;

	if (obj instanceof Double || obj instanceof BigDecimal) {

	    if (num.doubleValue() == 0) {
		opcode = NumOpcode.ZERO;
		ret = new byte[1];
		ret[0] = opcode;
		return ret;
	    }
	    opcode = NumOpcode.DOUBLE;
	    ret = new byte[9];
	    PacketUtil.writeDouble(offset, num.doubleValue(), ret);
	} else if (obj instanceof Float) {

	    if (num.floatValue() == 0) {
		opcode = NumOpcode.ZERO;
		ret = new byte[1];
		ret[0] = opcode;
		return ret;
	    }

	    opcode = NumOpcode.FLOAT;
	    ret = new byte[5];
	    PacketUtil.writeFloat(offset, num.floatValue(), ret);
	} else {
	    // 修正负数判断 。。。。
	    long value = num.longValue();
	    if (value < 0) {
		value = (long) -value;
	    }

	    if (value == 0) {
		opcode = NumOpcode.ZERO;
		ret = new byte[1];
		ret[0] = opcode;
		return ret;
	    }

	    if (value > PacketUtil.BIT_32) {
		opcode = NumOpcode.LONG;
		ret = new byte[9];
		PacketUtil.writeLong(offset, num.longValue(), ret);
	    } else if (value > PacketUtil.BIT_16) {
		opcode = NumOpcode.INT;
		ret = new byte[5];
		PacketUtil.writeInt(offset, num.intValue(), ret);
	    } else if (value > PacketUtil.BIT_8) {
		opcode = NumOpcode.SHORT;
		ret = new byte[3];
		PacketUtil.writeShort(offset, num.shortValue(), ret);
	    } else {
		opcode = NumOpcode.BYTE;
		ret = new byte[2];
		PacketUtil.writeByte(offset, num.byteValue(), ret);
	    }
	}
	ret[0] = opcode;
	return ret;
    }

    @Override
    public Object decode(Type type, byte[] bytes) {
	byte opcode = bytes[0];
	Number ret = null;
	int offset = 1;
	switch (opcode) {
	case NumOpcode.ZERO:
	    if (TypeUtils.isAssignable(type, Integer.class)) {
		return 0;
	    } else if (TypeUtils.isAssignable(type, Long.class)) {
		return 0L;
	    } else if (TypeUtils.isAssignable(type, Double.class)) {
		return 0d;
	    } else if (TypeUtils.isAssignable(type, Float.class)) {
		return 0f;
	    } else if (TypeUtils.isAssignable(type, Short.class)) {
		return (short) 0;
	    } else if (TypeUtils.isAssignable(type, Byte.class)) {
		return (byte) 0;
	    } else if (TypeUtils.isAssignable(type, AtomicLong.class)) {
		return new AtomicLong();
	    } else if (TypeUtils.isAssignable(type, AtomicInteger.class)) {
		return new AtomicInteger();
	    } else if (TypeUtils.isAssignable(type, BigDecimal.class)) {
		return new BigDecimal(0);
	    }
	    break;
	case NumOpcode.BYTE: // byte
	    ret = PacketUtil.readByte(offset, bytes);
	    break;
	case NumOpcode.SHORT: // short
	    ret = PacketUtil.readShort(offset, bytes);
	    break;
	case NumOpcode.INT: // int
	    ret = PacketUtil.readInt(offset, bytes);
	    break;
	case NumOpcode.LONG: // long
	    ret = PacketUtil.readLong(offset, bytes);
	    break;
	case NumOpcode.DOUBLE: // double
	    ret = PacketUtil.readDouble(offset, bytes);
	    break;
	case NumOpcode.FLOAT: // float
	    ret = PacketUtil.readFloat(offset, bytes);
	    break;
	}
	if (TypeUtils.isAssignable(type, AtomicLong.class)) {
	    ret = new AtomicLong(ret.longValue());
	} else if (TypeUtils.isAssignable(type, AtomicInteger.class)) {
	    ret = new AtomicInteger(ret.intValue());
	} else if (TypeUtils.isAssignable(type, BigDecimal.class)) {
	    ret = new BigDecimal(ret.longValue());
	}
	return ret;
    }

    @Override
    public byte getType() {
	return TypeCode.NUM;
    }

}
