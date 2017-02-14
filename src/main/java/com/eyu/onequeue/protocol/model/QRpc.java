package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.util.PacketUtil;

/**
 * <p>
 * 2byte model 1byte command + 4byte indexs len + indexs + params + 1byte
 * compress
 * </p>
 * 
 * @author solq
 */
public class QRpc implements IRecycle, IByte {

    private short model;
    private byte command;

    private byte[] indexs;
    private byte[] params;
    private byte compress;

    public byte[] toBytes() {
	byte[] ret = new byte[toSize()];
	int offset = 0;
	PacketUtil.writeShort(offset, model, ret);
	PacketUtil.writeByte(offset += Short.BYTES, command, ret);
	PacketUtil.writeInt(offset += Byte.BYTES, indexs.length, ret);
	PacketUtil.writeBytes(offset += Integer.BYTES, indexs, ret);
	PacketUtil.writeBytes(offset += indexs.length, params, ret);
	PacketUtil.writeByte(offset += params.length, compress, ret);

	return ret;
    }

    @Override
    public int toSize() {
	return 2 + 1 + 4 + indexs.length + params.length + 1;
    }

    public static QRpc toObject(byte[] bytes) {
	int offset = 0;
	short model = PacketUtil.readShort(offset, bytes);
	byte command = PacketUtil.readByte(offset += Short.BYTES, bytes);
	final int indexLen = PacketUtil.readInt(offset += Byte.BYTES, bytes);
	byte[] indexs = PacketUtil.readBytes(offset += Integer.BYTES, indexLen, bytes);
	byte[] params = PacketUtil.readBytes(offset += indexs.length, bytes.length - offset - 1, bytes);

	byte compress = PacketUtil.readByte(offset += params.length, bytes);

	return of(model, command, indexs, params, compress);
    }

    public static QRpc of(short model, byte command, byte[] indexs, byte[] params, byte compress) {
	QRpc ret = new QRpc();
	ret.model = model;
	ret.command = command;
	ret.indexs = indexs;
	ret.params = params;
	ret.compress = compress;
	return ret;
    }

    // getter

    public byte[] getParams() {
	return params;
    }

    public short getModel() {
	return model;
    }

    public byte getCommand() {
	return command;
    }

    public byte[] getIndexs() {
	return indexs;
    }

    public byte getCompress() {
	return compress;
    }

    @Override
    public void recycle() {
	params = null;
    }

}
