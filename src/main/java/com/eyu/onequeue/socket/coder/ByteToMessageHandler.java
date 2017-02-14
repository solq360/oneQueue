package com.eyu.onequeue.socket.coder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QJsonException;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.socket.model.IByteDecoder;
import com.eyu.onequeue.util.NettyUtil;
import com.eyu.onequeue.util.NettyUtil.CLOSE_SOURCE;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author solq
 **/
public class ByteToMessageHandler extends ByteToMessageDecoder implements IByteDecoder<QPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteToMessageHandler.class);

    private IByteDecoder<ByteBuf> decoder = new com.eyu.onequeue.socket.coder.ByteToMessageDecoder(QMConfig.getInstance().PACKET_MAX_LENGTH);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	try {
	    Object decoded = decode(ctx, in);
	    if (decoded != null) {
		out.add(decoded);
	    }
	} catch (QSocketException | QJsonException e) {
	    LOGGER.error("解码包失败 : {} ", e);
	    NettyUtil.closeChannel(CLOSE_SOURCE.SOCKET_ERROR, false, ctx.channel());
	} catch (Exception e) {
	    LOGGER.error("解码包未知异常 : {} ", e);
	    NettyUtil.closeChannel(CLOSE_SOURCE.UNKNOWN, false, ctx.channel());
	    throw e;
	}
    }

    @Override
    public QPacket decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
	ByteBuf byteBuf = decoder.decode(ctx, in);
	if (byteBuf == null || !byteBuf.isReadable()) {
	    return null;
	}
	if (byteBuf.readableBytes() < QPacket.PACK_FIXED_LENG) {
	    return null;
	}
	final short headFlag = byteBuf.readShort();
	final int packetLen = byteBuf.readInt();
	final int endLen = 1;

	if (headFlag != QMConfig.getInstance().getPacketHeadFlag(packetLen)) {
	    throw new QSocketException(QCode.SOCKET_ERROR_PCKET_FLAG, "解码包头标识不对");
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
	    throw new QSocketException(QCode.SOCKET_ERROR_PCKET_FLAG, "解码包尾标识不对");
	}
	byteBuf.readerIndex(mark);
	QPacket ret = QPacket.of(byteBuf, packetLen);
	byteBuf.skipBytes(endLen);
	return ret;
    }

}
