package com.eyu.onequeue.protocol.model;

import com.eyu.onequeue.util.PacketUtil;

/**
 * 
 * @author solq
 */
public class QRpc implements IRelease, IByte {
    /** 指令号 **/
    private short command;
    /** 参数信息 **/
    private byte[] params;
    /** 转发目标nodes **/
    private byte[] nodes;

    public byte[] toBytes() {
	final int nodeLen = nodes == null ? 0 : nodes.length;
	byte[] ret = new byte[toSize()];
	int offset = 0;
	PacketUtil.writeShort(offset, command, ret);
	PacketUtil.writeShort(offset += Short.BYTES, (short) params.length, ret);
	PacketUtil.writeBytes(offset += Short.BYTES, params, ret);
	PacketUtil.writeShort(offset += params.length, (short) nodeLen, ret);
	if (nodeLen > 0) {
	    PacketUtil.writeBytes(offset += Short.BYTES, nodes, ret);
	}
	return ret;
    }

    @Override
    public int toSize() {
	final int nodeLen = nodes == null ? 0 : nodes.length;
	return Short.BYTES + Short.BYTES + params.length + Short.BYTES + nodeLen;
    }

    public static QRpc toObject(byte[] bytes) {
	int offset = 0;
	short command = PacketUtil.readShort(offset, bytes);
	final short paramsLen = PacketUtil.readShort(offset += Short.BYTES, bytes);
	byte[] params = PacketUtil.readBytes(offset += Short.BYTES, paramsLen, bytes);
	final short nodesLen = PacketUtil.readShort(offset += params.length, bytes);
	byte[] nodes = null;
	if (nodesLen > 0) {
	    nodes = PacketUtil.readBytes(offset += Short.BYTES, nodesLen, bytes);
	}
	return of(command, params, nodes);
    }

    public static QRpc of(short command, byte[] params, byte[] nodes) {
	QRpc ret = new QRpc();
	ret.command = command;
	ret.params = params;
	ret.nodes = nodes;
	return ret;
    }

    // getter

    public short getCommand() {
	return command;
    }

    public byte[] getParams() {
	return params;
    }

    public byte[] getNodes() {
	return nodes;
    }

    @Override
    public void release() {
	params = null;
	nodes = null;
    }

}
