package com.eyu.onequeue.socket.coder;

import com.eyu.onequeue.socket.handler.QDispenseHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author solq
 **/
@Sharable
public class QMessageHandler extends ChannelDuplexHandler {

    private QDispenseHandler dispenseHandler;

    public QMessageHandler(QDispenseHandler dispenseHandler) {
	this.dispenseHandler = dispenseHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	dispenseHandler.doConnect(ctx);
	super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	dispenseHandler.doReceive(msg, ctx);
	super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	dispenseHandler.doClose(ctx);
	super.channelInactive(ctx);
    }
}
