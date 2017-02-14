package com.eyu.onequeue.socket.model;

import java.util.Collections;
import java.util.Map;

import com.eyu.onequeue.QMConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;

/**
 * @author solq
 **/
public class NettyClientConfig {
    private int workerGroupThread;
    private Map<ChannelOption<?>, ?> sessionOptions;
    private Class<? extends SocketChannel> acceptor;
    private long connectTimeout;

    // getter
    public Map<ChannelOption<?>, ?> getSessionOptions() {
	if (sessionOptions == null) {
	    return null;
	}
	return Collections.unmodifiableMap(sessionOptions);
    }

    public int getWorkerGroupThread() {
	return workerGroupThread;
    }

    public Class<? extends SocketChannel> getAcceptor() {
	return acceptor;
    }

    public long getConnectTimeout() {
	return connectTimeout;
    }

    public static NettyClientConfig of() {
	return QMConfig.getInstance().buildClientConfig();
    }

    public static NettyClientConfig of(long connectTimeout, int workerGroupThread, Map<ChannelOption<?>, ?> sessionOptions, Class<? extends SocketChannel> acceptor) {
	NettyClientConfig ret = new NettyClientConfig();
	ret.connectTimeout = connectTimeout;
	ret.workerGroupThread = workerGroupThread;
	ret.sessionOptions = sessionOptions;
	ret.acceptor = acceptor;
	return ret;
    }

}
