package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.anno.QModel;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

import io.netty.buffer.ByteBuf;

/***
 * 包 格式 [sn] + [c] + [b] +[sid] {@link PacketUtil#PACK_FIXED_LENG}
 * 
 * @author solq
 */
public class QPacket implements IRelease, IByte {
    /** 序号 用于包ID，解决冥等 **/
    private long sn;
    /** sessionId **/
    private long sid;
    /** opCode **/
    private byte c;
    /** 内容 **/
    private byte[] b;

    @Override
    public void release() {
	b = null;
    }

    @Override
    public byte[] toBytes() {
	final int len = toSize();
	byte[] ret = new byte[len];
	int offset = 0;
	PacketUtil.writeLong(offset, sn, ret);
	PacketUtil.writeByte(offset += Long.BYTES, c, ret);
	PacketUtil.writeBytes(offset += Byte.BYTES, b, ret);
	PacketUtil.writeLong(offset += b.length, sid, ret);
	return ret;
    }

    @Override
    public int toSize() {
	return PacketUtil.PACK_FIXED_LENG + b.length;
    }

    public int writeBytes(ByteBuf byteBuf) {
	byteBuf.writeLong(sn);
	byteBuf.writeByte(c);
	byteBuf.writeBytes(b);
	byteBuf.writeLong(sid);
	return toSize();
    }

    public void writeToByteBuf(ByteBuf byteBuf) {
	final int packetLen = toSize();
	byteBuf.writeShort(QMConfig.getInstance().getPacketHeadFlag(packetLen));
	byteBuf.writeInt(packetLen);
	writeBytes(byteBuf);
	byteBuf.writeByte(QMConfig.getInstance().getPacketEndFlag(packetLen));
    }

    public QProduce toProduce() {
	QProduce ret = SerialUtil.readZipValue(b, QProduce.class);
	return ret;
    }

    public QConsume toConsume() {
	QConsume ret = QConsume.byte2Object(SerialUtil.zip(b));
	return ret;
    }

    public QRpc toRpc() {
	QRpc ret = QRpc.toObject(b);
	return ret;
    }

    public short toCode() {
	return PacketUtil.readShort(0, b);
    }

    public void responseCode(short code) {
	c = QModel.QCODE;
	b = new byte[2];
	PacketUtil.writeShort(0, code, b);
    }

    // static
    public static QPacket of(Object data) {
	if (data instanceof QProduce) {
	    return of((QProduce) data);
	}
	if (data instanceof QRpc) {
	    return of((QRpc) data);
	}
	if (data instanceof QConsume) {
	    return of((QConsume) data);
	}
	if (data instanceof byte[]) {
	    return of((byte[]) data);
	}
	if (data instanceof Integer) {
	    return of((Integer) data);
	}
	if (data instanceof QPacket) {
	    return (QPacket) data;
	}
	throw new RuntimeException("未支持类型 ：" + data.getClass());
    }

    public static QPacket of(ByteBuf byteBuf, int packetLen) {
	long sn = byteBuf.readLong();
	byte c = byteBuf.readByte();
	byte[] b = new byte[packetLen - PacketUtil.PACK_FIXED_LENG];
	byteBuf.readBytes(b);
	long sid = byteBuf.readLong();
	return of(c, sn, sid, b);
    }

    public static QPacket of(byte[] bytes) {
	int offset = 0;
	long sn = PacketUtil.readLong(offset, bytes);
	byte c = PacketUtil.readByte(offset += Long.BYTES, bytes);
	byte[] b = PacketUtil.readBytes(offset += Byte.BYTES, bytes.length - PacketUtil.PACK_FIXED_LENG, bytes);
	long sid = PacketUtil.readLong(offset += b.length, bytes);
	return of(c, sn, sid, b);
    }

    public static QPacket of(short code) {
	byte[] b = new byte[2];
	PacketUtil.writeShort(0, code, b);
	long sn = PacketUtil.getSn();
	return of(QModel.QCODE, sn, -1, b);
    }

    public static QPacket of(QRpc obj) {
	byte[] b = obj.toBytes();
	long sn = PacketUtil.getSn();
	return of(QModel.QRPC, sn, -1, b);
    }

    public static QPacket of(QProduce obj) {
	byte[] b = SerialUtil.writeValueAsZipBytes(obj);
	long sn = PacketUtil.getSn();
	return of(QModel.QPRODUCE, sn, -1, b);
    }

    public static QPacket of(QConsume obj) {
	byte[] b = obj.toBytes();
	b = SerialUtil.zip(b);
	long sn = PacketUtil.getSn();
	return of(QModel.QCONSUME, sn, -1, b);
    }

    public static QPacket of(byte c, long sn, long sid, byte[] body) {
	QPacket ret = new QPacket();
	ret.c = c;
	ret.sn = sn;
	ret.sid = sid;
	ret.b = body;
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

    public void setSid(long sid) {
	this.sid = sid;
    }

}
