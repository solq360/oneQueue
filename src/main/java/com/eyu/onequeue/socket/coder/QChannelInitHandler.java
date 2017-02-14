package com.eyu.onequeue.socket.coder;

import com.eyu.onequeue.socket.handler.QDispenseHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
/**
 * @author solq
 **/
public class QChannelInitHandler extends ChannelInitializer<SocketChannel> {
    private ChannelHandler handler;

    public QChannelInitHandler(QDispenseHandler dispenseHandler) {
	this.handler = new QMessageHandler(dispenseHandler);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
	ch.pipeline().addLast("encode", new MessageToByteHandler());
	ch.pipeline().addLast("decode", new ByteToMessageHandler());
	ch.pipeline().addLast("handler", handler);
    }

}
