package com.eyu.onequeue.socket.service;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.socket.model.NettyServerConfig;

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
public class QNettyServer {
    private static final Logger logger = LoggerFactory.getLogger(QNettyServer.class);

    private volatile boolean closed = false;
    private volatile boolean initializer = false;

    private NettyServerConfig config;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelInitializer<SocketChannel> childHandler;
    private Channel channel;

    public static QNettyServer of(NettyServerConfig config, ChannelInitializer<SocketChannel> childHandler) {
	QNettyServer ret = new QNettyServer();
	ret.config = config;
	ret.childHandler = childHandler;
	return ret;
    }

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

	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
	    ChannelFuture f = bootstrap.bind(config.getPort());
	    channel = f.sync().channel();
 	    logger.error("start QNettyServer : " + config.getPort());
	    if (config.isBlock()) {
		channel.closeFuture().sync();
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    logger.error("start error QNettyServer : {} ", e);
	}
    }

    public void close() {
	if (closed) {
	    return;
	}
	logger.error("close QNettyServer : " + config.getPort());
	initializer = false;
	closed = true;
 	workerGroup.shutdownGracefully();
	bossGroup.shutdownGracefully();
    }

}
