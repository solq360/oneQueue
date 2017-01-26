package com.eyu.onequeue.socket.model;

import java.util.Set;

import com.eyu.onequeue.protocol.model.QPacket;

/**
 * 订阅节点服务器
 **/
public class QNode {
    /**
     * 订阅集合
     **/
    private Set<String> topics;
    /**
     * session
     **/
    private QSession session;

    public void send(QPacket packet) {
	// TODO Auto-generated method stub

    }

    public void sendSync(QPacket packet) {
	// TODO Auto-generated method stub

    }
}
