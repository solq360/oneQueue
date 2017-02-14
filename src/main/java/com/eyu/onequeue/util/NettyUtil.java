package com.eyu.onequeue.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.socket.model.QNode;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

/**
 * @author solq
 */
public abstract class NettyUtil {

    public final static AttributeKey<QNode> ATTR_NODE = AttributeKey.valueOf("ATTR_NODE");

    public static enum CLOSE_SOURCE {
	SERVER_STOP, SOCKET_ERROR, NORMAL, REPLACE, UNKNOWN

    }

    public static void closeChannel(CLOSE_SOURCE opCode, boolean sync, Channel channel) {
	if (channel == null || !channel.isActive()) {
	    return;
	}
	ChannelFuture future = channel.close();
	if (!sync) {
	    return;
	}
	try {
	    future.await();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public static void setNode(AttributeMap ctx, QNode node) {
	Attribute<QNode> attr = ctx.attr(ATTR_NODE);
	attr.set(node);
    }

    public static QNode getNode(AttributeMap ctx) {
	Attribute<QNode> attr = ctx.attr(ATTR_NODE);
	QNode node = attr.get();
	return node;
    }

    public static Channel connect(Bootstrap bootstrap, SocketAddress socketAddress, long timeOut) {
	ChannelFuture future = bootstrap.connect(socketAddress);
	try {
	    boolean done = future.await(timeOut);
	    if (!done) {
		FormattingTuple msg = MessageFormatter.format("与服务器[{}]连接超时!!!", socketAddress.toString());
		throw new QSocketException(QCode.SOCKET_ERROR_CONNECT_TIMEOUT, msg.getMessage(), null);
	    }
	} catch (InterruptedException e) {
	    throw new QSocketException(QCode.SOCKET_ERROR_CONNECT_TIMEOUT, null, e);
	}

	Channel ret = future.channel();
	if (ret == null || !ret.isActive()) {
	    FormattingTuple msg = MessageFormatter.format("连接服务器[{}]失败!!!", socketAddress.toString());
	    throw new QSocketException(QCode.SOCKET_ERROR_CONNECT, msg.getMessage(), null);
	}
	return ret;
    }

    public static Channel connect(Bootstrap bootstrap, String address, long timeOut) {
	SocketAddress socketAddress = toInetSocketAddress(address);
	return connect(bootstrap, socketAddress, timeOut);
    }

    public static InetSocketAddress toInetSocketAddress(String text) {
	if (text == null || text.isEmpty()) {
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
    private static int parsePort(String s) {
	try {
	    return Integer.parseInt(s);
	} catch (NumberFormatException nfe) {
	    throw new IllegalArgumentException("无效的端口值: " + s);
	}
    }

    public static String getAddress(InetSocketAddress inetSocketAddress) {
	return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }

    public static InetSocketAddress newAddress(InetSocketAddress inetSocketAddress) {
	return InetSocketAddress.createUnresolved(inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort());
    }
}
