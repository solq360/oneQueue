package com.eyu.onequeue.socket.service;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.socket.model.IQSocket;
import com.eyu.onequeue.socket.model.NettyClientConfig;
import com.eyu.onequeue.socket.model.NettyServerConfig;
import com.eyu.onequeue.socket.model.QNode;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * @author solq
 **/
public class QNettyServer implements IQSocket {
    private static final Logger logger = LoggerFactory.getLogger(QNettyServer.class);

    private volatile boolean closed = false;
    private volatile boolean initializer = false;

    private NettyServerConfig config;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelInitializer<SocketChannel> childHandler;
    private Channel channel;

    private QNettyClient nettyClient;

    public static QNettyServer of(NettyServerConfig serverConfig, NettyClientConfig clientConfig, ChannelInitializer<SocketChannel> childHandler) {
	QNettyServer ret = new QNettyServer();
	ret.config = serverConfig;
	ret.childHandler = childHandler;
	ret.nettyClient = QNettyClient.of(clientConfig, childHandler);
	return ret;
    }

    @Override
    public void start() {
	if (initializer) {
	    return;
	}
	initializer = true;
	closed = false;

	bossGroup = new NioEventLoopGroup(config.getBossGroupThread());
	workerGroup = new NioEventLoopGroup(config.getWorkerGroupThread());
	try {
	    ServerBootstrap bootstrap = new ServerBootstrap();
	    bootstrap.group(bossGroup, workerGroup).channel(config.getAcceptor()).childHandler(childHandler);

	    if (config.getSessionOptions() != null) {
		for (Entry<?, ?> e : config.getSessionOptions().entrySet()) {
		    @SuppressWarnings("unchecked")
		    ChannelOption<Object> key = (ChannelOption<Object>) e.getKey();
		    Object value = e.getValue();
		    bootstrap.option(key, value);
		}
	    }

	    if (config.getChildSessionOptions() != null) {
		for (Entry<?, ?> e : config.getChildSessionOptions().entrySet()) {
		    @SuppressWarnings("unchecked")
		    ChannelOption<Object> key = (ChannelOption<Object>) e.getKey();
		    Object value = e.getValue();
		    bootstrap.childOption(key, value);
		}
	    }

	    ChannelFuture f = bootstrap.bind(config.getPort());
	    channel = f.sync().channel();
	    logger.error("start QNettyServer : " + config.getPort());
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    logger.error("start error QNettyServer : {} ", e);
	}
	if (nettyClient != null) {
	    nettyClient.start();
	}
    }

    @Override
    public void sync() {
	try {
	    channel.closeFuture().sync();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void close() {
	if (closed) {
	    return;
	}
	logger.error("close QNettyServer : " + config.getPort());
	initializer = false;
	closed = true;
	workerGroup.shutdownGracefully();
	bossGroup.shutdownGracefully();

	if (nettyClient != null) {
	    nettyClient.close();
	}
    }

    @Override
    public void send(QNode node, Object message) {
	node.send(message, null);
    }
    @Override
    public void sendAll(Object message, IQCallback<?> cb) {
	nettyClient.sendAll(message, cb);
    }
    @Override
    public <T> IQCallback<T> send(QNode node, Object message, IQCallback<T> cb) {
	return node.send(message, cb);
    }

    @Override
    public QNode connect(String address, String id) {
	return nettyClient.connect(address, id);
    }

   

}
