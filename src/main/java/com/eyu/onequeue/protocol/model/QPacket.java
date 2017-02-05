package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.protocol.anno.QModel;
import com.eyu.onequeue.store.model.QResult;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

/***
 * 包 格式 [sn] + [c] + [b] +[sid] {@link PacketUtil#PACK_FIXED_LENG}
 * 
 * @author solq
 */
public class QPacket {
    /** 序号 用于包ID，解决冥等 **/
    private long sn;
    /** sessionId **/
    private long sid;
    /** code 编码 (格式: model *10  ) **/
    private short c;
    /** 内容 **/
    private byte[] b;
 
    public static QPacket produce2Packet(Object obj) {
 	short c =(short) (obj.getClass().getAnnotation(QModel.class).value() *10);
	byte[] b = SerialUtil.writeValueAsBytes(obj);
	 b = SerialUtil.zip(b);
	long sn = PacketUtil.getSn();
	long sid = PacketUtil.getSessionId();
	return of(c, sn, sid, b);
    }
    public static QPacket result2Packet(QResult obj) {
 	short c =(short) (obj.getClass().getAnnotation(QModel.class).value() *10);
	byte[] b = obj.toBytes();
 	b = SerialUtil.zip(b);
	long sn = PacketUtil.getSn();
	long sid = PacketUtil.getSessionId();
	return of(c, sn, sid, b);
    }

    public static QPacket byte2Packet(byte[] bytes) {
	long sn = PacketUtil.readLong(0, bytes);
	short c = PacketUtil.readShort(Long.BYTES, bytes);
	byte[] b = PacketUtil.readBytes(Long.BYTES + Short.BYTES, bytes.length - PacketUtil.PACK_FIXED_LENG, bytes);
	long sid = PacketUtil.readLong(Long.BYTES + Short.BYTES + b.length, bytes);
	return of(c, sn, sid, b);
    }
    
    public byte[] toBytes() {
	final int len = PacketUtil.PACK_FIXED_LENG + b.length;
	byte[] ret = new byte[len];
	PacketUtil.writeLong(0, sn, ret);
	PacketUtil.writeShort(Long.BYTES, c, ret);
	PacketUtil.writeBytes(Long.BYTES + Short.BYTES, b, ret);
	PacketUtil.writeLong(Long.BYTES + Short.BYTES + b.length, sid, ret);
	return ret;
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
