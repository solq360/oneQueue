package com.eyu.onequeue.socket.handle;

import java.net.SocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class TestChannelHandler extends ChannelDuplexHandler {

    
    void doTestTask(ChannelHandlerContext ctx){
	 final ByteBuf time = ctx.alloc().buffer(4); // (2)
	        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

	        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
	        f.addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) {
	                assert f == future;
	                ctx.close();
	            }
	        }); // (4)
    }
    
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future) throws Exception {
	System.out.println("server bind");
	super.bind(ctx, localAddress, future);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server read");
	super.read(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelActive");
	doTestTask(ctx);
	super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelInactive");
	super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	System.out.println("server channelRead");
	super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelReadComplete");
	super.channelReadComplete(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelRegistered");
	super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelUnregistered");
	super.channelUnregistered(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server channelWritabilityChanged");

	super.channelWritabilityChanged(ctx);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
	System.out.println("server close");
	super.close(ctx, future);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
	System.out.println("server connect");
	super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
	System.out.println("server deregister");
	super.deregister(ctx, future);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
	System.out.println("server disconnect");
	super.disconnect(ctx, future);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	System.out.println("server exceptionCaught");
	super.exceptionCaught(ctx, cause);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server flush");
	super.flush(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server handlerAdded");
	super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
	System.out.println("server handlerRemoved");
	super.handlerRemoved(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	System.out.println("server userEventTriggered");
	super.userEventTriggered(ctx, evt);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
	System.out.println("server write");
	super.write(ctx, msg, promise);
    }

}
