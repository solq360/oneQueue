package com.eyu.onequeue.socket.model;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface IByteDecoder<T> {

    /**
     * 数据包解码
     * 
     * @param ctx
     * @param in
     * @return
     * @throws Exception
     */
    T decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception;

}
