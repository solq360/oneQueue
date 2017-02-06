package com.eyu.onequeue.socket.model;

import java.util.Map;

import com.eyu.onequeue.QMConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.ServerSocketChannel;

public class NettyClientConfig {
    private int workerGroupThread;
    private Map<ChannelOption<?>, ?> sessionOptions;
    private Class<? extends ServerSocketChannel> acceptor;
    private long connectTimeout;

    // getter
    public Map<ChannelOption<?>, ?> getSessionOptions() {
	return sessionOptions;
    }

    public int getWorkerGroupThread() {
	return workerGroupThread;
    }

    public Class<? extends ServerSocketChannel> getAcceptor() {
	return acceptor;
    }

    public long getConnectTimeout() {
	return connectTimeout;
    }

    public static NettyClientConfig of() {
	return QMConfig.getInstance().buildClientConfig();
    }

    public static NettyClientConfig of(int workerGroupThread, Map<ChannelOption<?>, ?> sessionOptions, Class<? extends ServerSocketChannel> acceptor) {
	NettyClientConfig ret = new NettyClientConfig();
	ret.workerGroupThread = workerGroupThread;
	ret.sessionOptions = sessionOptions;
	ret.acceptor = acceptor;
	return ret;
    }

}
