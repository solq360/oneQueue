package com.eyu.onequeue.socket.coder;

import com.eyu.onequeue.socket.model.IByteDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/**
 * @author solq
 **/
public class ByteToMessageDecoder extends LengthFieldBasedFrameDecoder implements IByteDecoder<ByteBuf> {

    public ByteToMessageDecoder(int maxFrameLength) {
	super(maxFrameLength, 2, 4, 1, 0);
    }

    @Override
    public ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
	ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
	return byteBuf;
    }

}
