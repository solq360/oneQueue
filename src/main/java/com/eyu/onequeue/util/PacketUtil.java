package com.eyu.onequeue.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QSocketException;

/***
 * @author solq
 */
public abstract class PacketUtil {
    // 用long类型做 id 如果每秒10W处理，可以N天才轮回一次
    private final static AtomicLong sn = new AtomicLong();
    private final static long SN_MAX = Long.MAX_VALUE ^ 0x0;
    public final static byte BYTE_TRUE = 1;
    public final static byte BYTE_FALSE = 0;
    public final static String CHARSET = "UTF-8";

    public final static byte BIT_8 = Byte.MAX_VALUE;
    public final static short BIT_16 = Short.MAX_VALUE;
    public final static int BIT_32 = Integer.MAX_VALUE;
    public final static long BIT_64 = Long.MAX_VALUE;

    public static byte[] getStringBytes(String str) {
	try {
	    return str.getBytes(CHARSET);
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    public static String byteToString(byte[] bytes) {
	try {
	    return new String(bytes, CHARSET);
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    public static Class<?> byteToClass(byte[] bytes) {
	try {
	    return Class.forName(new String(bytes));
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

    public static long getSn() {
	long next = sn.getAndIncrement();
	if (next > SN_MAX) {
	    sn.lazySet(0);
	}
	return next;
    }

    public static Double toMSize(int length) {
	double ret = length / 1024d / 1024d;
	BigDecimal bg = new BigDecimal(ret);
	ret = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	return ret;
    }

    public static Number autoReadNum(int offset, byte[] bytes) {
	byte bit = bytes[offset];
	offset++;
	Number ret = null;
	switch (bit) {
	case 9:
	    ret = readLong(offset, bytes);
	    break;
	case 5:
	    ret = readInt(offset, bytes);
	    break;
	case 3:
	    ret = readShort(offset, bytes);
	    break;
	case 2:
	    ret = readByte(offset, bytes);
	    break;
	case 1:
	    ret = (byte) 0;
	    break;
	default:
	    throw new QSocketException(QCode.SOCKET_UNKNOWN_OPCODE, "auto bytes unknown opcode :" + bit);
	}

	return ret;
    }

    public static byte autoWriteNum(int offset, Number v, byte[] ret) {
	byte bit = getAutoWriteLen(v);
	writeByte(offset, bit, ret);
	offset++;
	switch (bit) {
	case 9:
	    writeLong(offset, v.longValue(), ret);
	    break;
	case 5:
	    writeInt(offset, v.intValue(), ret);
	    break;
	case 3:
	    writeShort(offset, v.shortValue(), ret);
	    break;
	case 2:
	    writeByte(offset, v.byteValue(), ret);
	    break;
	case 1:
	    break;
	default:
	    throw new QSocketException(QCode.SOCKET_UNKNOWN_OPCODE, "auto bytes unknown opcode :" + bit);
	}
	return bit;
    }

    public static byte getAutoWriteLen(Number value) {
	long v = value.longValue();
	if (v < 0) {
	    v = -v;
	}
	byte bit = 0;
	if (v == 0) {
	    bit = 1;
	} else if (v > BIT_32) {
	    bit = 9;
	} else if (v > BIT_16) {
	    bit = 5;
	} else if (v > BIT_8) {
	    bit = 3;
	} else {
	    bit = 2;
	}
	return bit;
    }

    public static void writeFloat(int offset, float n, byte[] ret) {
	int v = Float.floatToIntBits(n);
	writeInt(offset, v, ret);
    }

    public static double readFloat(int offset, byte[] bytes) {
	return Float.intBitsToFloat(readInt(offset, bytes));
    }

    public static void writeDouble(int offset, double n, byte[] ret) {
	long v = Double.doubleToLongBits(n);
	writeLong(offset, v, ret);
    }

    public static double readDouble(int offset, byte[] bytes) {
	return Double.longBitsToDouble(readLong(offset, bytes));
    }

    public static boolean readBoolean(int offset, byte[] bytes) {
	byte v = bytes[offset];
	return v == 0 ? false : true;
    }

    public static void writeBoolean(int offset, boolean v, byte[] ret) {
	ret[offset] = (byte) (v ? 0 : 1);
    }

    public static void writeLong(int offset, long v, byte[] ret) {
	ret[offset] = (byte) (v >> 56);
	ret[offset + 1] = (byte) (v >> 48);
	ret[offset + 2] = (byte) (v >> 40);
	ret[offset + 3] = (byte) (v >> 32);
	ret[offset + 4] = (byte) (v >> 24);
	ret[offset + 5] = (byte) (v >> 16);
	ret[offset + 6] = (byte) (v >> 8);
	ret[offset + 7] = (byte) (v);
    }

    public static void writeInt(int offset, int v, byte[] ret) {
	ret[offset] = (byte) (v >> 24);
	ret[offset + 1] = (byte) (v >> 16);
	ret[offset + 2] = (byte) (v >> 8);
	ret[offset + 3] = (byte) (v);
    }

    public static void writeShort(int offset, short v, byte[] ret) {
	ret[offset] = (byte) (v >> 8);
	ret[offset + 1] = (byte) (v);
    }

    public static void writeBytes(int offset, byte[] v, byte[] ret) {
	for (int i = 0; i < v.length; offset++, i++) {
	    ret[offset] = v[i];
	}
    }

    public static void writeByte(int offset, byte b, byte[] ret) {
	ret[offset] = b;
    }

    public static byte readByte(int offset, byte[] ret) {
	return ret[offset];
    }

    public static long readLong(int offset, byte[] bytes) {

	long s0 = bytes[offset] & 0xFF;
	long s1 = bytes[offset + 1] & 0xFF;
	long s2 = bytes[offset + 2] & 0xFF;
	long s3 = bytes[offset + 3] & 0xFF;
	long s4 = bytes[offset + 4] & 0xFF;
	long s5 = bytes[offset + 5] & 0xFF;
	long s6 = bytes[offset + 6] & 0xFF;
	long s7 = bytes[offset + 7] & 0xFF;
	// 操作顺序不能乱 先 &清理溢出 然后循环位移累加上一次值
	return s0 << 56 | s1 << 48 | s2 << 40 | s3 << 32 | s4 << 24 | s5 << 16 | s6 << 8 | s7;
    }

    public static int readInt(int offset, byte[] bytes) {
	int s0 = bytes[offset] & 0xFF;
	int s1 = bytes[offset + 1] & 0xFF;
	int s2 = bytes[offset + 2] & 0xFF;
	int s3 = bytes[offset + 3] & 0xFF;
	return s0 << 24 | s1 << 16 | s2 << 8 | s3;
    }

    public static short readShort(int offset, byte[] bytes) {
	return (short) ((bytes[offset] & 0xFF) << 8 | bytes[offset + 1] & 0xFF);
    }

    public static byte[] readBytes(int offset, int len, byte[] bytes) {
	byte[] ret = new byte[len];
	for (int i = 0; i < len; offset++, i++) {
	    ret[i] = bytes[offset];
	}
	return ret;
    }

    public static String readString(int offset, int len, byte[] bytes) {
	return new String(readBytes(offset, len, bytes));
    }
}
