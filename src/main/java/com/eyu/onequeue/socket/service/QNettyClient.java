package com.eyu.onequeue.socket.service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.socket.model.NettyClientConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

public class QNettyClient {

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
    }

    public void connect(String address) {
	try {
	    SocketAddress socketAddress = toInetSocketAddress(address);
	    ChannelFuture future = bootstrap.connect(socketAddress);
	    boolean done = future.await(config.getConnectTimeout());
	    if (!done) {
		FormattingTuple msg = MessageFormatter.format("与服务器[{}]连接超时!!!", address);
		throw QSocketException.of(QSocketException.class, QCode.SOCKET_ERROR_CONNECT_TIMEOUT, msg, null);
	    }
	    Channel channel = future.channel();
	    if (channel == null || !channel.isActive()) {
		FormattingTuple msg = MessageFormatter.format("连接服务器[{}]失败!!!", address);
		throw QSocketException.of(QSocketException.class, QCode.SOCKET_ERROR_CONNECT, msg, null);
	    }

	    logger.warn("start QNettyClient : " + socketAddress);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    logger.error("start error QNettyClient : {} ", e);
	}
    }

    public void sync() {
	while (!closed) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
	    }
	}
    }

    /** 连接地址 */
    private InetSocketAddress toInetSocketAddress(String text) {
	if (StringUtils.isEmpty(text)) {
	    throw new IllegalArgumentException("无效的地址字符串: " + text);
	}

	int colonIndex = text.lastIndexOf(":");
	if (colonIndex > 0) {
	    String host = text.substring(0, colonIndex);
	    if (!"*".equals(host)) {
		int port = parsePort(text.substring(colonIndex + 1));
		return new InetSocketAddress(host, port);
	    }
	}

	int port = parsePort(text.substring(colonIndex + 1));
	return new InetSocketAddress(port);
    }

    /** 获取端口值 */
    private int parsePort(String s) {
	try {
	    return Integer.parseInt(s);
	} catch (NumberFormatException nfe) {
	    throw new IllegalArgumentException("无效的端口值: " + s);
	}
    }

    public void close() {
	if (closed) {
	    return;
	}
	logger.error("close QNettyClient : ");
	initializer = false;
	closed = true;

	workerGroup.shutdownGracefully();
    }

}