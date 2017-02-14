package com.eyu.onequeue.socket.model;

import java.util.Collections;
import java.util.Map;

import com.eyu.onequeue.QMConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.ServerSocketChannel;

/**
 * @author solq
 **/
public class NettyServerConfig {
    private int port;

    private int bossGroupThread;
    private int workerGroupThread;
    private Map<ChannelOption<?>, ?> sessionOptions;
    private Map<ChannelOption<?>, ?> childSessionOptions;
    private Class<? extends ServerSocketChannel> acceptor;

    // getter
    public int getPort() {
	return port;
    }

    public Map<ChannelOption<?>, ?> getSessionOptions() {
	if (sessionOptions == null) {
	    return null;
	}
	return Collections.unmodifiableMap(sessionOptions);
    }

    public Map<ChannelOption<?>, ?> getChildSessionOptions() {
	if (childSessionOptions == null) {
	    return null;
	}
	return Collections.unmodifiableMap(childSessionOptions);
    }

    public int getBossGroupThread() {
	return bossGroupThread;
    }

    public int getWorkerGroupThread() {
	return workerGroupThread;
    }

    public Class<? extends ServerSocketChannel> getAcceptor() {
	return acceptor;
    }

    public static NettyServerConfig of() {
	return QMConfig.getInstance().buildServerConfig();
    }

    public static NettyServerConfig of(int port, int bossGroupThread, int workerGroupThread, Map<ChannelOption<?>, ?> sessionOptions, Map<ChannelOption<?>, ?> childSessionOptions,
	    Class<? extends ServerSocketChannel> acceptor) {
	NettyServerConfig ret = new NettyServerConfig();
	ret.port = port;
	ret.bossGroupThread = bossGroupThread;
	ret.workerGroupThread = workerGroupThread;
	ret.sessionOptions = sessionOptions;
	ret.childSessionOptions = childSessionOptions;
	ret.acceptor = acceptor;
	return ret;
    }

}
