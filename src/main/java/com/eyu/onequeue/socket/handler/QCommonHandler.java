package com.eyu.onequeue.socket.handler;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.protocol.model.QRpc;
import com.eyu.onequeue.protocol.model.QSubscribe;
import com.eyu.onequeue.push.QPushManager;
import com.eyu.onequeue.rpc.service.QRpcEnhanceService;
import com.eyu.onequeue.socket.model.IQReceiveHandler;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.socket.model.QSession;
import com.eyu.onequeue.socket.service.QNodeFactory;
import com.eyu.onequeue.store.model.IQStoreService;
import com.eyu.onequeue.store.service.QStoreService;
import com.eyu.onequeue.util.NettyUtil;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author solq
 */
public class QCommonHandler extends QDispenseHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(QCommonHandler.class);

    protected IQStoreService storeService = new QStoreService();
    protected QPushManager pushManager = new QPushManager(storeService);

    void buildClientHandler() {
	// 消费端处理逻辑
	// this.consumeHandler = new IQReceiveHandler<QConsume>() {
	// @Override
	// public void onReceive(ChannelHandlerContext ctx, QPacket packet,
	// QConsume body) {
	//
	// Map<String, String> dirs =
	// QMConfig.getInstance().CONSUME_LOG_SAVE_DIRS;
	// String logName = dirs.get(body.getTopic());
	// body.foreachMessageData((list) -> {
	// for (QMessage<?, ?> m : list) {
	// System.out.println("onReceive : " + m.toString());
	// }
	// });
	// }
	// };
    }

    void buildServerHandler() {
	// 订阅处理
	this.subscribeHandler = new IQReceiveHandler<Collection<QSubscribe>>() {
	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket packet, Collection<QSubscribe> body) {
		QNode node = NettyUtil.getNode(ctx);
		for (QSubscribe sub : body) {
		    System.out.println("topic" + sub.toString());
		    pushManager.registerNode(sub.getTopic(), sub.getGroupId(), node);
		}
	    }
	};
	// 中转服处理逻辑 将数据持久化
	this.produceHandler = new IQReceiveHandler<QProduce>() {
	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket packet, QProduce body) {
		storeService.save(body.getT(), body.getB());
	    }
	};
	// 消费端处理逻辑
	// this.consumeHandler = new IQReceiveHandler<QConsume>() {
	// @Override
	// public void onReceive(ChannelHandlerContext ctx, QPacket packet,
	// QConsume body) {
	//
	// Map<String, String> dirs =
	// QMConfig.getInstance().CONSUME_LOG_SAVE_DIRS;
	// String logName = dirs.get(body.getTopic());
	// body.foreachMessageData((list) -> {
	// for (QMessage<?, ?> m : list) {
	// System.out.println("onReceive : " + m.toString());
	// }
	// });
	// }
	// };
    }

    void buildCommandHandler() {
	// 响应回调部分
	this.responseHandler = new IQReceiveHandler<Short>() {

	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket packet, Short body) {
		QNode node = NettyUtil.getNode(ctx);
		switch (body) {
		case QCode.SUCCEED:
		    node.getCallbackManager().doReceiveSucceed(packet);
		    break;
		default:
		    node.getCallbackManager().doReceiveError(packet);
		    break;
		}
	    }
	};

	//////////////////// 生成 node//////////////////////
	this.beforeHandler = new IQReceiveHandler<Void>() {
	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket packet, Void body) {
		QNode node = NettyUtil.getNode(ctx);
		if (node == null) {
		    node = QNodeFactory.get(packet.getSid());
		    if (node != null && !node.isClosed()) {
			throw new QSocketException(QCode.SOCKET_ERROR_ALREADY_SESSION);
		    }
		    node = QNode.of(QSession.of(packet.getSid()), ctx.channel());
		    QNodeFactory.registerNode(node);
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(" 生成 node : " + node.getSession().getId() + "  packet sid :" + packet.getSid());
		    }

		} else if (node.getSession().getId() != packet.getSid()) {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(" session id 不相等 : " + node.getSession().getId() + "  packet sid :" + packet.getSid());
		    }
		    throw new QSocketException(QCode.SOCKET_ERROR_NOEQ_SESSION);
		}
	    }

	};
	this.closeHandler = new IQReceiveHandler<Void>() {

	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket nul, Void body) {
		QNode node = NettyUtil.getNode(ctx);
		if (node == null) {
		    return;
		}
		QNodeFactory.removeNode(node.toId());
	    }
	};
	this.rpcHandler = new IQReceiveHandler<QRpc>() {
	    
	    @Override
	    public void onReceive(ChannelHandlerContext ctx, QPacket packet, QRpc body) {
		Object value = QRpcEnhanceService.getFactory().invoke(body);
	    }
	};
    }

    @Override
    public void start() {
	storeService.start();
	pushManager.start();
    }

    @Override
    public void close() {
	pushManager.close();
	storeService.close();
    }

    // getter

    public IQStoreService getStoreService() {
	return storeService;
    }

    public QPushManager getPushManager() {
	return pushManager;
    }

}
