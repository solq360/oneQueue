package com.eyu.onequeue.socket.handler;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QException;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.protocol.anno.QOpCode;
import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.protocol.model.QRpc;
import com.eyu.onequeue.protocol.model.QSubscribe;
import com.eyu.onequeue.socket.model.IQReceiveHandler;
import com.eyu.onequeue.util.NettyUtil;
import com.eyu.onequeue.util.NettyUtil.CLOSE_SOURCE;
import com.eyu.onequeue.util.PoolUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 分发接收消息
 * 
 * @author solq
 */
public abstract class QDispenseHandler implements IQService {
    protected final static Logger LOGGER = LoggerFactory.getLogger(QDispenseHandler.class);
    //应用层逻辑
    protected IQReceiveHandler<QProduce> produceHandler;
    protected IQReceiveHandler<QConsume> consumeHandler;
    protected IQReceiveHandler<Collection<QSubscribe>> subscribeHandler;
    protected IQReceiveHandler<Short> responseHandler;
    protected IQReceiveHandler<QRpc> rpcHandler;

    //通信层逻辑
    protected IQReceiveHandler<Void> acceptHandler;
    protected IQReceiveHandler<Void> closeHandler;

    protected IQReceiveHandler<Void> beforeHandler;
    protected IQReceiveHandler<Void> afterHandler;

    protected final static ExecutorService pool = PoolUtil.createPool(QMConfig.getInstance().POOL_REQUEST_CORE, 60, "messageDispense");

    public void doClose(ChannelHandlerContext ctx) {
	if (closeHandler == null) {
	    return;
	}
	pool.execute(() -> {
	    closeHandler.onReceive(ctx, null, null);
	});
    }

    public void doConnect(ChannelHandlerContext ctx) {
	if (acceptHandler == null) {
	    return;
	}
	pool.execute(() -> {
	    acceptHandler.onReceive(ctx, null, null);
	});
    }

    public void doReceive(Object msg, ChannelHandlerContext ctx) {
	if (msg instanceof QPacket) {
	    pool.execute(() -> {
		doReceive0((QPacket) msg, ctx);
	    });
	}
    }

    private void doReceive0(QPacket packet, ChannelHandlerContext ctx) {

	boolean response = packet.hasStatus(QPacket.MASK_RESPONSE);
	try {
	    if (beforeHandler != null) {
		beforeHandler.onReceive(ctx, packet, null);
	    }
	    // 业务处理
	    final short c = packet.getC();
	    switch (c) {
	    case QOpCode.QPRODUCE:
		if (produceHandler != null) {
		    QProduce produce = packet.toProduce();
		    produceHandler.onReceive(ctx, packet, produce);
		}
		break;
	    case QOpCode.QCONSUME:
		if (consumeHandler != null) {
		    QConsume qConsume = packet.toConsume();
		    consumeHandler.onReceive(ctx, packet, qConsume);
		}
		break;
	    case QOpCode.QSUBSCIBE:
		if (subscribeHandler != null) {
		    Collection<QSubscribe> qSubscribe = packet.toSubscribe();
		    subscribeHandler.onReceive(ctx, packet, qSubscribe);
		}
		break;
	    case QOpCode.QRPC:
		response = false;
		// 业务回写请求
		if (rpcHandler != null) {
		    QRpc qRpc = packet.toRpc();
		    rpcHandler.onReceive(ctx, packet, qRpc);
		}
		break;
	    case QOpCode.QCODE:
		response = false;
		// 响应请求
		if (responseHandler != null) {
		    short code = packet.toCode();
		    responseHandler.onReceive(ctx, packet, code);
		}
		break;
	    default:
		throw new QSocketException(QCode.SOCKET_UNKNOWN_OPCODE, " opCode : " + c);
	    }

	    if (afterHandler != null) {
		afterHandler.onReceive(ctx, packet, null);
	    }
	    // 响应成功
	    packet.responseCode(QCode.SUCCEED);
	} catch (QSocketException e) {
	    // socket异常 关闭连接
	    packet.responseCode(e.getCode());
	    LOGGER.error("QSocketException  code {} error : {}", e.getCode(), e);
	    if (response) {
		ChannelFuture future = ctx.channel().writeAndFlush(packet);
		future.addListener(new GenericFutureListener<Future<? super Void>>() {
		    @Override
		    public void operationComplete(Future<? super Void> future) throws Exception {
			NettyUtil.closeChannel(CLOSE_SOURCE.SOCKET_ERROR, false, ctx.channel());
		    }
		});
	    } else {
		NettyUtil.closeChannel(CLOSE_SOURCE.SOCKET_ERROR, false, ctx.channel());
	    }
	    return;
	} catch (QException e) {
	    // 业务异常 响应失败
	    packet.responseCode(e.getCode());
	    LOGGER.error("QException  code {} error : {}", e.getCode(), e);
	} catch (Exception e) {
	    // 未知异常 响应失败
	    packet.responseCode(QCode.ERROR_UNKNOWN);
	    LOGGER.error("UNKNOWNException  error : {}", e);
	}
	if (response) {
	    ctx.channel().writeAndFlush(packet);
	}
    }

}
