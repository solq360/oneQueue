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
 
    public byte[] toBytes() {
	byte[] ret = new byte[toSize()];
	int offset = 0;
	PacketUtil.writeShort(offset, model, ret);
	PacketUtil.writeByte(offset += Short.BYTES, command, ret);

	if (indexs.length > 0) {
	    byte autoLen = PacketUtil.autoWriteNum(offset += Byte.BYTES, indexs.length, ret);
	    PacketUtil.writeBytes(offset += autoLen, indexs, ret);
	    PacketUtil.writeBytes(offset += indexs.length, params, ret);
 	}

	return ret;
    }

    @Override
    public int toSize() {
	int last = 0;
	if (indexs.length > 0) {
	    last = PacketUtil.getAutoWriteLen(indexs.length) + indexs.length + params.length;
	}
	return 2 + 1 + last;
    }

    public static QRpc toObject(byte[] bytes) {
	int offset = 0;
	short model = PacketUtil.readShort(offset, bytes);
	byte command = PacketUtil.readByte(offset += Short.BYTES, bytes);
	byte[] indexs = null;
	byte[] params = null;
 	offset += Byte.BYTES;
	if (offset == bytes.length) {
	    indexs = new byte[0];
	    params = new byte[0];
 	} else {
	    final byte autoLen = bytes[offset];
	    final int indexLen = PacketUtil.autoReadNum(offset, bytes).intValue();
	    indexs = PacketUtil.readBytes(offset += autoLen, indexLen, bytes);
	    params = PacketUtil.readBytes(offset += indexs.length, bytes.length - offset, bytes);
 	}

	return of(model, command, indexs, params);
    }

    public static QRpc of(short model, byte command, byte[] indexs, byte[] params ) {
	QRpc ret = new QRpc();
	ret.model = model;
	ret.command = command;
	ret.indexs = indexs;
	ret.params = params;
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
 

    @Override
    public void recycle() {
	params = null;
    }

}
