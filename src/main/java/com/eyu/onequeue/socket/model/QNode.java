package com.eyu.onequeue.socket.model;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.callback.service.QCallbackManager;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QException;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.protocol.model.IRecycle;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.util.NettyUtil;
import com.eyu.onequeue.util.NettyUtil.CLOSE_SOURCE;
import com.eyu.onequeue.util.QFactoryUtil;

import io.netty.channel.Channel;

/**
 * 节点服务器<br>
 * 应用层使用
 * 
 * @author solq
 **/
public class QNode implements IRecycle {
    private final static Logger LOGGER = LoggerFactory.getLogger(QNode.class);

    /**
     * session会话，记录通信层属性
     **/
    private QSession session;
    /**
     * message cb 维护消息回调
     **/
    private QCallbackManager callbackManager;
    /**
     * netty channel
     **/
    private Channel channel;

    private InetSocketAddress address;

    public static QNode of(QSession session, Channel channel) {
	QNode ret = new QNode();
	ret.session = session;
	ret.callbackManager = new QCallbackManager();
	ret.channel = channel;
	ret.address = (InetSocketAddress) channel.remoteAddress();
	NettyUtil.setNode(channel, ret);
	return ret;
    }

    public void replace(QNode old) {
	session.replace(old.session);
	callbackManager = old.callbackManager;

	NettyUtil.closeChannel(CLOSE_SOURCE.REPLACE, false, old.channel);

	bindNode(old.channel, channel);

	// recycle
	old.callbackManager = null;
	old.recycle();
    }

    public <T> IQCallback<T> send(Object data, IQCallback<T> cb) {
	QPacket packet = QPacket.of(data);
	packet.setSid(session.getId());
	IQCallback<T> ret = callbackManager.doSend(packet, cb);
	try {
	    channel.writeAndFlush(packet);
	} catch (QException e) {
	    if (cb == null) {
		throw e;
	    }
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("sendSendError : {}  {}", e.getCode(), e);
	    }
	    callbackManager.doSendError(packet, e.getCode());
	} catch (Exception e) {
	    if (cb == null) {
		throw e;
	    }
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("sendSendError : {}  ", e);
	    }
	    callbackManager.doSendError(packet, QCode.ERROR_UNKNOWN);
	}
	return ret;
    }

    public void connect() {
	if (!isClosed()) {
	    return;
	}
	try {
	    Channel newChannel = NettyUtil.connect(QFactoryUtil.getValue(QFactoryUtil.CLIENT_DEFAULT_BOOTSTRAP), address, QMConfig.getInstance().NETTY_CLIENT_CONNECT_TIMEOUT);
	    bindNode(channel, newChannel);
	    channel = newChannel;
	} catch (Exception e) {
	    throw new QSocketException(QCode.SOCKET_ERROR_CONNECT, null, e);
	}

    }

    public void close(boolean sync) {
	NettyUtil.closeChannel(CLOSE_SOURCE.NORMAL, sync, channel);
    }

    @Override
    public void recycle() {
	if (session != null) {
	    session.recycle();
	}
	if (callbackManager != null) {
	    callbackManager.recycle();
	}
	NettyUtil.setNode(channel, null);
	NettyUtil.closeChannel(CLOSE_SOURCE.SERVER_STOP, false, channel);
	channel = null;
    }

    private void bindNode(Channel oldChannel, Channel newChannel) {
	if (oldChannel != null) {
	    NettyUtil.setNode(oldChannel, null);
	}
	NettyUtil.setNode(newChannel, this);
    }

    public boolean isClosed() {
	if (channel != null && channel.isActive()) {
	    return false;
	}
	return true;
    }

    public Long toId() {
	return session.getId();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((session == null) ? 0 : session.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	QNode other = (QNode) obj;
	if (session == null) {
	    if (other.session != null)
		return false;
	} else if (!session.equals(other.session))
	    return false;
	return true;
    }

    // getter
    public QSession getSession() {
	return session;
    }

    public Channel getChannel() {
	return channel;
    }

    public QCallbackManager getCallbackManager() {
	return callbackManager;
    }

    public InetSocketAddress getAddress() {
	return address;
    }

}
