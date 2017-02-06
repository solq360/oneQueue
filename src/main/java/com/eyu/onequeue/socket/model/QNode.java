package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.callback.service.QCallbackManager;
import com.eyu.onequeue.protocol.model.IRelease;
import com.eyu.onequeue.protocol.model.QPacket;

/**
 * 节点服务器<br>
 * 应用层使用
 * 
 * @author solq
 **/
public class QNode implements IRelease {

    /**
     * session会话，维护通信层
     **/
    private QSession session;
    /**
     * message cb 维护消息回调
     **/
    private QCallbackManager callbackManager;

    public static QNode of(QSession session, QCallbackManager callbackManager) {
	QNode ret = new QNode();
	ret.session = session;
	ret.callbackManager = callbackManager;
	return ret;
    }

    public static QNode replace(QSession session, QNode node) {
	QNode ret = new QNode();
	ret.session = session;
	ret.callbackManager = node.callbackManager;
	node.callbackManager = null;
	node.release();
	return ret;
    }

    public <T> QResult<T> send(Object data) {
	QPacket packet = QPacket.of(data);
	packet.setSid(session.getSid());

	return null;
    }

    public <T> QResult<T> sendSync(Object data) {
	QPacket packet = QPacket.of(data);
	packet.setSid(session.getSid());
	return null;
    }

    @Override
    public void release() {
	if (session != null) {
	    session.release();
	}
	if (callbackManager != null) {
	    callbackManager.release();
	}
    }

}
