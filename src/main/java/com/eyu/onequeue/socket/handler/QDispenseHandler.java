package com.eyu.onequeue.socket.handler;

import java.util.concurrent.ExecutorService;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.protocol.anno.QModel;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.protocol.model.QRpc;
import com.eyu.onequeue.socket.model.IQResponseHandler;
import com.eyu.onequeue.util.PoolUtil;

import io.netty.channel.ChannelHandlerContext;

/**
 * 分发接收消息
 * 
 * @author solq
 */
public class QDispenseHandler {
    private IQResponseHandler<QProduce> produceHandler;
    private IQResponseHandler<QConsume> consumeHandler;
    private IQResponseHandler<QRpc> rpcHandler;
    private IQResponseHandler<Integer> codeHandler;

    private static ExecutorService pool = PoolUtil.createPool(QMConfig.getInstance().POOL_REQUEST_CORE, 60, "messageDispense");

    public static QDispenseHandler of(IQResponseHandler<QProduce> produceHandler, IQResponseHandler<QConsume> consumeHandler, IQResponseHandler<QRpc> rpcHandler, IQResponseHandler<Integer> codeHandler) {
	QDispenseHandler ret = new QDispenseHandler();
	ret.produceHandler = produceHandler;
	ret.consumeHandler = consumeHandler;
	ret.codeHandler = codeHandler;
	ret.rpcHandler = rpcHandler;
	return ret;
    }

    public void doReceive(Object msg, ChannelHandlerContext ctx) {
	if (msg instanceof QPacket) {
	    pool.execute(() -> {
		_doReceive((QPacket) msg, ctx);
	    });
	}
    }

    void _doReceive(QPacket qpacket, ChannelHandlerContext ctx) {
	boolean response = true;
	try {
	    // 业务处理
	    final short c = qpacket.getC();
	    switch (c) {
	    case QModel.QPRODUCE:
		if (produceHandler != null) {
		    QProduce produce = qpacket.toProduce();
		    produceHandler.onReceive(qpacket, produce);
		}
		break;
	    case QModel.QCONSUME:
		if (consumeHandler != null) {
		    QConsume qConsume = qpacket.toConsume();
		    consumeHandler.onReceive(qpacket, qConsume);
		}
		break;
	    case QModel.QRPC:
		response = false;
		// 业务回写请求
		if (rpcHandler != null) {
		    QRpc qRpc = qpacket.toRpc();
		    rpcHandler.onReceive(qpacket, qRpc);
		}
		break;
	    case QModel.QCODE:
		response = false;
		// 响应请求
		if (codeHandler != null) {
		    int code = qpacket.toCode();
		    codeHandler.onReceive(qpacket, code);
		}
		break;
	    default:
		throw new RuntimeException("unknown model");
	    }
	    // 响应成功
	    qpacket.responseCode(QCode.SUCCEED);
	} catch (Exception e) {
	    // 响应失败
	    qpacket.responseCode(QCode.ERROR_UNKNOWN);
	}
	if (response) {
	    ctx.writeAndFlush(qpacket);
	}
    }
}
