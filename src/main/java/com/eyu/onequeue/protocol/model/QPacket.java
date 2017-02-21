package com.eyu.onequeue.protocol.model;

import java.util.Collection;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.anno.QOpCode;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

import io.netty.buffer.ByteBuf;

/***
 * 包 格式 [sn] + [c] + [b] +[sid]
 * 
 * c [0000 1111] 16个协议
 * 
 * @author solq
 */
public class QPacket implements IRecycle, IByte {

    /**
     * 包固定长度
     */
    public final static int PACK_FIXED_LENG = Long.BYTES + Short.BYTES + Long.BYTES;
    /**
     * 响应掩码 [0001 0000]
     */
    public final static short MASK_RESPONSE = 0x10;

    /**
     * 压缩掩码[0010 0000]
     */
    public final static short MASK_COMPRESS = 0x20;

    public final static int MASK_OPCODE = MASK_RESPONSE | MASK_COMPRESS;

    /** 序号 用于包ID，解决冥等 **/
    private long sn;
    /** sessionId **/
    private long sid;
    /** opCode **/
    private short c;
    /** 内容 **/
    private byte[] b;
    /** 临时数据 **/
    private Object tmpData;

    @Override
    public void recycle() {
	b = null;
	tmpData = null;
    }

    @Override
    public int toSize() {
	return QPacket.PACK_FIXED_LENG + b.length;
    }

    public int writeBytes(ByteBuf byteBuf) {
	byteBuf.writeLong(sn);
	byteBuf.writeShort(c);
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

    /////////////////// toObject///////////////////////
    @Override
    public byte[] toBytes() {
	final int len = toSize();
	byte[] ret = new byte[len];
	int offset = 0;
	PacketUtil.writeLong(offset, sn, ret);
	PacketUtil.writeShort(offset += Long.BYTES, c, ret);
	PacketUtil.writeBytes(offset += Short.BYTES, b, ret);
	PacketUtil.writeLong(offset += b.length, sid, ret);
	return ret;
    }

    public QProduce toProduce() {
	QProduce ret = toObject();
	if (ret == null) {
	    ret = QProduce.of(getBytes());
	    tmpData = ret;
	}
	return ret;
    }

    public QConsume toConsume() {
	QConsume ret = toObject();
	if (ret == null) {
	    ret = QConsume.byte2Object(getBytes());
	    tmpData = ret;
	}
	return ret;
    }

    public Collection<QSubscribe> toSubscribe() {
	Collection<QSubscribe> ret = toObject();
	if (ret == null) {
	    ret = SerialUtil.readValue(getBytes(), SerialUtil.subType);
	    tmpData = ret;
	}
	return ret;
    }

    public QRpc toRpc() {
	QRpc ret = toObject();
	if (ret == null) {
	    ret = QRpc.toObject(getBytes());
	    tmpData = ret;
	}
	return ret;
    }

    public short toCode() {
	Short ret = toObject();
	if (ret == null) {
	    ret = PacketUtil.readShort(0, b);
	    tmpData = ret;
	}
	return ret;
    }

    @SuppressWarnings("unchecked")
    <T> T toObject() {
	return (T) tmpData;
    }

    byte[] getBytes() {
	byte[] bytes = null;
	if (hasStatus(MASK_COMPRESS)) {
	    bytes = SerialUtil.unZip(b);
	} else {
	    bytes = b;
	}
	return bytes;
    }

    public void responseCode(short code) {
	c = QOpCode.QCODE;
	b = new byte[2];
	tmpData = code;
	PacketUtil.writeShort(0, code, b);
    }

    public void setStatus(short value) {
	c |= value;
    }

    public boolean hasStatus(short value) {
	return (c & value) == value;
    }

    // static
    @SuppressWarnings("unchecked")
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
	if (TypeUtils.isAssignable(data.getClass(), SerialUtil.subType.getType())) {
	    return of((Collection<QSubscribe>) data);
	}
	throw new RuntimeException("未支持类型 ：" + data.getClass());
    }

    public static QPacket of(ByteBuf byteBuf, int packetLen) {
	long sn = byteBuf.readLong();
	short c = byteBuf.readShort();
	byte[] b = new byte[packetLen - QPacket.PACK_FIXED_LENG];
	byteBuf.readBytes(b);
	long sid = byteBuf.readLong();
	return of(c, sn, sid, null, b);
    }

    public static QPacket of(byte[] bytes) {
	int offset = 0;
	long sn = PacketUtil.readLong(offset, bytes);
	short c = PacketUtil.readShort(offset += Long.BYTES, bytes);
	byte[] b = PacketUtil.readBytes(offset += Short.BYTES, bytes.length - QPacket.PACK_FIXED_LENG, bytes);
	long sid = PacketUtil.readLong(offset += b.length, bytes);
	return of(c, sn, sid, null, b);
    }

    public static QPacket of(short code) {
	byte[] b = new byte[2];
	PacketUtil.writeShort(0, code, b);
	long sn = PacketUtil.getSn();
	return of(QOpCode.QCODE, sn, -1, null, b);
    }

    public static QPacket of(QRpc obj) {
	byte[] b = obj.toBytes();
	long sn = PacketUtil.getSn();
	return of(QOpCode.QRPC, sn, -1, null, b);
    }

    public static QPacket of(QProduce obj) {
	byte[] b = obj.toBytes();
	long sn = PacketUtil.getSn();
	return of(QOpCode.QPRODUCE, sn, -1, null, b);
    }

    public static QPacket of(QConsume obj) {
	byte[] b = obj.toBytes();
	long sn = PacketUtil.getSn();
	return of(QOpCode.QCONSUME, sn, -1, null, b);
    }

    public static QPacket of(Collection<QSubscribe> obj) {
	byte[] b = SerialUtil.writeValueAsBytes(obj);
	long sn = PacketUtil.getSn();
	return of(QOpCode.QSUBSCIBE, sn, -1, null, b);
    }

    public static QPacket of(short c, long sn, long sid, Object value, byte[] body) {
	QPacket ret = new QPacket();
	ret.c = c;
	ret.sn = sn;
	ret.sid = sid;
	// 未压缩，处理压缩
	if (!ret.hasStatus(MASK_COMPRESS) && body.length >= QMConfig.getInstance().COMPRESS_SIZE) {
	    body = SerialUtil.zip(body);
	    ret.setStatus(MASK_COMPRESS);
	}
	ret.b = body;
	ret.tmpData = value;
	return ret;
    }

    // getter

    public short getC() {
	return (short) (c & ~QPacket.MASK_OPCODE);
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
