package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.MQServerConfig;
import com.eyu.onequeue.util.PackageUtil;
import com.eyu.onequeue.util.SerialUtil;

/***
 * 包 格式 [sn] + [c] + [b] +[sid] {@link PackageUtil#PACK_FIXED_LENG}
 * 
 * @author solq
 */
public class QPacket {
    /** 序号 用于包ID，解决冥等 **/
    private long sn;
    /** sessionId **/
    private long sid;
    /** code 编码 (格式: model *10 + 0/1 0-正常 1-压缩 ) **/
    private short c;
    /** 内容 **/
    private byte[] b;

    public static QPacket object2Package(Object obj) {
	byte[] b = SerialUtil.writeValueAsBytes(obj);
	short c = MQServerConfig.MESSAGE_CODE_NORMAL;
	if (b.length > MQServerConfig.MESSAGE_ZIP_VALUE_UPPER) {
	    b = SerialUtil.zip(b);
	    c = MQServerConfig.MESSAGE_CODE_ZIP;
	}

	long sn = PackageUtil.getSn();
	long sid = PackageUtil.getSessionId();
	return of(c, sn, sid, b);
    }

    public byte[] toBytes() {
	final int len = PackageUtil.PACK_FIXED_LENG + b.length;
	byte[] ret = new byte[len];
	PackageUtil.writeLong(0, sn, ret);
	PackageUtil.writeShort(Long.BYTES, c, ret);
	PackageUtil.writeBytes(Long.BYTES + Short.BYTES, b, ret);
	PackageUtil.writeLong(Long.BYTES + Short.BYTES + b.length, sid, ret);
	return ret;
    }

    public static QPacket byte2Package(byte[] bytes) {
	long sn = PackageUtil.readLong(0, bytes);
	short c = PackageUtil.readShort(Long.BYTES, bytes);
	byte[] b = PackageUtil.readBytes(Long.BYTES + Short.BYTES, bytes.length - PackageUtil.PACK_FIXED_LENG, bytes);
	long sid = PackageUtil.readLong(Long.BYTES + Short.BYTES + b.length, bytes);
	return of(c, sn, sid, b);
    }

    // getter

    public short getC() {
	return c;
    }

    public long getSid() {
	return sid;
    }

    public long getSn() {
	return sn;
    }

    public byte[] getB() {
	return b;
    }

    public static QPacket of(short c, long sn, long sid, byte[] body) {
	QPacket ret = new QPacket();
	ret.c = c;
	ret.sn = sn;
	ret.sid = sid;
	ret.b = body;
	return ret;
    }

}
