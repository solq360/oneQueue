package com.eyu.onequeue.socket;

import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onqueue.socket.handle.TestChannelHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class QMessageServerHandler extends TestChannelHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	
	Channel   channel = ctx.channel();
          super.channelActive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	Channel   channel = ctx.channel();
	if(msg instanceof QPacket){
	    QPacket qpacket = (QPacket)msg;
	     //入内存 持久化  推送 业务
	}else{
	    //什么也不做
	}
         super.channelRead(ctx, msg);
    }
}
