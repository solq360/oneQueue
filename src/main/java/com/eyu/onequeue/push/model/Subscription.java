package com.eyu.onequeue.push.model;

import java.util.Set;

import com.eyu.onequeue.socket.model.QNode;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Subscription {
    /**
     * topic
     */
    private String topic;
    /**
     * groudId
     */
    private String groudId;
    /**
     * 该组已读取数据指针
     */
    private long offset;

    /**
     * 该组nodes
     */
    @JsonIgnore
    private Set<QNode> nodes;
}
