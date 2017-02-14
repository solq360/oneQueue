package com.eyu.onequeue.socket.service;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.socket.model.IQSocket;
import com.eyu.onequeue.socket.model.NettyClientConfig;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.socket.model.QSession;
import com.eyu.onequeue.util.NettyUtil;
import com.eyu.onequeue.util.QFactoryUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * @author solq
 **/
public class QNettyClient implements IQSocket {

    private static final Logger logger = LoggerFactory.getLogger(QNettyClient.class);

    private volatile boolean closed = false;
    private volatile boolean initializer = false;

    private NettyClientConfig config;

    private EventLoopGroup workerGroup;
    private ChannelInitializer<SocketChannel> handler;
    private Bootstrap bootstrap;

    public static QNettyClient of(NettyClientConfig config, ChannelInitializer<SocketChannel> handler) {
	QNettyClient ret = new QNettyClient();
	ret.config = config;
	ret.handler = handler;
	return ret;
    }

    @Override
    public void start() {
	if (initializer) {
	    return;
	}
	initializer = true;
	closed = false;

	workerGroup = new NioEventLoopGroup(config.getWorkerGroupThread());
	bootstrap = new Bootstrap();
	bootstrap.group(workerGroup).channel(config.getAcceptor()).handler(handler);
	if (config.getSessionOptions() != null) {
	    for (Entry<?, ?> e : config.getSessionOptions().entrySet()) {
		@SuppressWarnings("unchecked")
		ChannelOption<Object> key = (ChannelOption<Object>) e.getKey();
		Object value = e.getValue();
		bootstrap.option(key, value);
	    }
	}

	QFactoryUtil.putValue(QFactoryUtil.CLIENT_DEFAULT_BOOTSTRAP, bootstrap);
    }

    @Override
    public QNode connect(String address, String id) {

	if (id == null) {
	    id = address + "--" + QMConfig.getInstance().LOCALNAME;
	}

	QNode ret = QNodeFactory.get(id);
	if (ret != null) {
	    if (ret.isClosed()) {
		ret.connect();
		if (!ret.isClosed()) {
		    return ret;
		}
	    } else {
		return ret;
	    }
	}

	Channel channel = NettyUtil.connect(bootstrap, address, config.getConnectTimeout());
	ret = QNode.of(QSession.of(id), channel);
	QNodeFactory.registerNode(ret);
	logger.error("connect : " + address);
	return ret;
    }

    @Override
    public void sendAll(Object message, IQCallback<?> cb) {
	QNodeFactory.foreach((node) -> {
	    IQCallback<?> _cb = null;
	    // 多个只能处理简单的逻辑，无法处理复杂情况
	    if (cb != null) {
		_cb = new IQCallback<Void>() {

		    @Override
		    public void onSucceed(short code) {
			cb.onSucceed(code);
		    }

		    @Override
		    public void onReceiveError(short code) {
			cb.onReceiveError(code);
		    }

		    @Override
		    public void onSendError(short code) {
			cb.onSendError(code);
		    }
		};
	    }
	    node.send(message, _cb);
	});
    }

    @Override
    public void send(QNode node, Object message) {
	node.send(message, null);
    }

    @Override
    public <T> IQCallback<T> send(QNode node, Object message, IQCallback<T> cb) {
	return node.send(message, cb);
    }

    @Override
    public void sync() {
	while (!closed) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
	    }
	}
    }

    /** 连接地址 */

    public void close() {
	if (closed) {
	    return;
	}
	logger.error("close QNettyClient");
	initializer = false;
	closed = true;
	QNodeFactory.closeAll();
	QFactoryUtil.clear();
	workerGroup.shutdownGracefully();
    }

}