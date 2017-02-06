package com.eyu.onequeue.socket.coder;

import com.eyu.onequeue.socket.handler.QDispenseHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class QMessageHandler extends ChannelDuplexHandler {

    private QDispenseHandler dispenseHandler;

    public QMessageHandler(QDispenseHandler dispenseHandler) {
	this.dispenseHandler = dispenseHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	dispenseHandler.doReceive(msg, ctx);
	super.channelRead(ctx, msg);
    }
}
