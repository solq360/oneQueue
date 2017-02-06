package com.eyu.onequeue.push;

import java.util.LinkedList;
import java.util.List;

import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.socket.model.QNode;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 订阅对象
 * 
 * @author solq
 */
public class Subscribe {
	/**
	 * topic
	 */
	private String topic;
	/**
	 * groudId
	 */
	private String groupId;
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

	public void push(QConsume result) {
		QNode node = findNode();
		node.sendSync(QPacket.of(result));
		// 推送成功记录
		this.offset = Math.max(result.getOffset(), offset);
	}
	public void push() {
 
	}
	// 单双循环策略
	private QNode findNode() {
		if (nodes.size() == 1) {
			return nodes.get(0);
		}
		int i = pushIndex++ % nodes.size();
		return nodes.get(i);
	}

	

	public static Subscribe of(String topic, String groupId) {
		Subscribe ret = new Subscribe();
		ret.topic = topic;
		ret.groupId = groupId;

		return ret;
	}
	// getter

	public String getTopic() {
		return topic;
	}

	public List<QNode> getNodes() {
	    return nodes;
	}

	public String getGroupId() {
		return groupId;
	}

	public long getOffset() {
		return offset;
	}

	public int getPushIndex() {
		return pushIndex;
	}

}
