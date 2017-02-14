package com.eyu.onequeue.socket.coder;

import com.eyu.onequeue.protocol.model.QPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
/**
 * @author solq
 **/
public class MessageToByteHandler extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

	if (msg instanceof ByteBuf) {
	    out.writeBytes((ByteBuf) msg);
	} else if (msg instanceof byte[]) {
	    out.writeBytes((byte[]) msg);
	} else if (msg instanceof QPacket) {
	    QPacket qPacket = (QPacket) msg;
	    qPacket.writeToByteBuf(out);
	}
    }

}
