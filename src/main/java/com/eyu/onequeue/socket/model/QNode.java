package com.eyu.onequeue.socket.model;

import java.util.Set;

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
}
