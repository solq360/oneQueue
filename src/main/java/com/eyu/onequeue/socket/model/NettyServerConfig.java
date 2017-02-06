package com.eyu.onequeue.socket.model;

import java.util.Map;

import com.eyu.onequeue.QMConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.ServerSocketChannel;

public class NettyServerConfig {
    private int port;
    private boolean block;

    private int bossGroupThread;
    private int workerGroupThread;
    private Map<ChannelOption<?>, ?> sessionOptions;
    private Class<? extends ServerSocketChannel> acceptor;
    // getter
    public int getPort() {
	return port;
    }

    public boolean isBlock() {
	return block;
    }

    public Map<ChannelOption<?>, ?> getSessionOptions() {
	return sessionOptions;
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

    public static NettyServerConfig of(int port, boolean block, int bossGroupThread, int workerGroupThread, Map<ChannelOption<?>, ?> sessionOptions,Class<? extends ServerSocketChannel> acceptor) {
	NettyServerConfig ret = new NettyServerConfig();
	ret.port = port;
	ret.block = block;
	ret.bossGroupThread = bossGroupThread;
	ret.workerGroupThread = workerGroupThread;
	ret.sessionOptions = sessionOptions;
	ret.acceptor = acceptor;
	return ret;
    }

}
