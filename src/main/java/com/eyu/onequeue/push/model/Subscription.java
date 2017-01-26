package com.eyu.onequeue.push.model;

import java.util.LinkedList;
import java.util.List;

import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.store.model.QResult;
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
    private List<QNode> nodes = new LinkedList<>();

    @JsonIgnore
    private int pushIndex;

    public void addNode(QNode node) {
	if (nodes.contains(node)) {
	    return;
	}
	nodes.add(node);
    }

    public void removeNode(QNode node) {
	nodes.remove(node);
    }

    public void push(QResult result) {
	QNode node = findNode();
	node.sendSync(result.toPacket());
	// 推送成功记录
	this.offset = Math.max(result.getOffset(), offset);
    }

    // 单双循环策略
    private QNode findNode() {
	if (nodes.size() == 1) {
	    return nodes.get(0);
	}
	int i = pushIndex++ % nodes.size();
	return nodes.get(i);
    }

    public void push() {
	// TODO Auto-generated method stub

    }

}
