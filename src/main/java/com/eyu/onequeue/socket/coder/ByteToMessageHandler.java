package com.eyu.onequeue.socket.coder;

import java.util.List;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.socket.model.IByteDecoder;
import com.eyu.onequeue.util.PacketUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ByteToMessageHandler extends ByteToMessageDecoder implements IByteDecoder<QPacket> {

    private IByteDecoder<ByteBuf> decoder = new com.eyu.onequeue.socket.coder.ByteToMessageDecoder(QMConfig.getInstance().PACKET_MAX_LENGTH);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	Object decoded = decode(ctx, in);
	if (decoded != null) {
	    out.add(decoded);
	}
    }

    @Override
    public QPacket decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
	ByteBuf byteBuf = decoder.decode(ctx, in);
	if (byteBuf == null || !byteBuf.isReadable()) {
	    return null;
	}
	if (byteBuf.readableBytes() < PacketUtil.PACK_FIXED_LENG) {
	    return null;
	}
	final short headFlag = byteBuf.readShort();
	final int packetLen = byteBuf.readInt();
	final int endLen = 1;

	if (headFlag != QMConfig.getInstance().getPacketHeadFlag(packetLen)) {
	    throw new RuntimeException("解码包头标识不对");
	}

	// netty 处理过了
	// if (byteBuf.readableBytes() - endLen < packetLen) {
	// return null;
	// }
	// 优化，减少new byte[]
	final int mark = byteBuf.readerIndex();
	byteBuf.skipBytes(packetLen);
	final byte endFlag = byteBuf.readByte();
	if (endFlag != QMConfig.getInstance().getPacketEndFlag(packetLen)) {
	    throw new RuntimeException("解码包尾标识不对");
	}
	byteBuf.readerIndex(mark);
	QPacket ret = QPacket.of(byteBuf, packetLen);
	byteBuf.skipBytes(endLen);
	return ret;
    }

}
