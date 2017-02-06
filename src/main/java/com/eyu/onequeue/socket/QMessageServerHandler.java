package com.eyu.onequeue.socket;

import com.eyu.onequeue.protocol.anno.QModel;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.socket.model.IConsumeHandle;
import com.eyu.onequeue.socket.model.IProduceHandle;
import com.eyu.onequeue.store.QMStoreService;
import com.eyu.onqueue.socket.handle.TestChannelHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class QMessageServerHandler extends TestChannelHandler {

    private IProduceHandle produceHandle;
    private IConsumeHandle consumeHandle;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

	Channel channel = ctx.channel();
	super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	Channel channel = ctx.channel();
	if (msg instanceof QPacket) {
	    QPacket qpacket = (QPacket) msg;

	    try {
		//业务处理
		final short c = qpacket.getC();
		switch (c) {
		case QModel.QPRODUCE:
		    QProduce produce = qpacket.toProduce();
		    produceHandle.onSucceed(produce);
		    break;
		case QModel.QCONSUME:
		    QConsume qConsume=  qpacket.toConsume();
		    consumeHandle.onSucceed(qConsume);
		    break;
		default:
		    throw new RuntimeException("unknown model");
		}

		// 响应成功
		
	    } catch (Exception e) {
		// 响应失败
	    }

	} else {
	    // 什么也不做
	}
	super.channelRead(ctx, msg);
    }
}
