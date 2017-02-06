package com.eyu.onequeue.socket.handler;

import com.eyu.onequeue.socket.coder.ByteToMessageHandler;
import com.eyu.onequeue.socket.coder.MessageToByteHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class QChannelInitHandler extends ChannelInitializer<SocketChannel> {
    private ChannelHandler handler;

    public QChannelInitHandler(ChannelHandler handler) {
	this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
	ch.pipeline().addLast("encode", new MessageToByteHandler());
	ch.pipeline().addLast("decode", new ByteToMessageHandler());
	ch.pipeline().addLast("handler", handler);
    }

}
