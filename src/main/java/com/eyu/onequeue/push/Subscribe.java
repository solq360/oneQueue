package com.eyu.onequeue.push;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.exception.QException;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.store.model.IQStoreService;
import com.eyu.onequeue.store.model.QQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 订阅对象
 * 
 * @author solq
 */
public class Subscribe {
    private final static Logger LOGGER = LoggerFactory.getLogger(Subscribe.class);

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

    public synchronized void addNode(QNode node) {
	if (nodes.contains(node)) {
	    return;
	}
	nodes.add(node);
    }

    public synchronized void removeNode(QNode node) {
	nodes.remove(node);
    }

    public synchronized boolean nodeIsEmpty() {
	for (QNode node : nodes) {
	    if (!node.isClosed()) {
		return false;
	    }
	}
	return true;
    }

    public synchronized void push(QPacket packet, long offset) {
	QNode node = findNode();
	if (node == null) {
	    return;
	}
	IQCallback<Void> cb = node.send(packet, new IQCallback<Void>() {
	    private final long sendOffset = offset;

	    @Override
	    public void onSucceed(short code) {
		record(sendOffset);
	    }
	});
	// 优化
	cb.setSendPacket(null);
	// 阻塞结果
	try {
	    cb.get();
	} catch (QException e) {
	    LOGGER.error("push error : {} ,{}", e.getCode(), e);
	} catch (Exception e) {

	}
    }

    public synchronized void push(IQStoreService storeService) {
	if (nodeIsEmpty()) {
	    return;
	}
	 QConsume consume = storeService.query(QQuery.of(topic, offset));
	    if (offset >= consume.getO() || consume.toBytes().length == 0) {
		return;
	    }
	// server push
	if (QMConfig.getInstance().SERVER_MODEL) {
	    push(QPacket.of(consume), consume.getO());
	} else {
	    // client again send transfer error
	    QProduce produce = consume.toProduce();
	    push(QPacket.of(produce), produce.getOffset());
	}
    }

    // 推送成功记录
    private synchronized void record(long sendOffset) {
	offset = Math.max(sendOffset, offset);
    }

    // 单双循环策略
    private QNode findNode() {
	for (int i = 0; i < nodes.size(); i++) {
	    int h = pushIndex++ % nodes.size();
	    QNode node = nodes.get(h);
	    if (!node.isClosed()) {
		return node;
	    }
	}
	return null;
    }

    public static Subscribe of(String topic, String groupId) {
	Subscribe ret = new Subscribe();
	ret.topic = topic;
	ret.groupId = groupId;
	return ret;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
	result = prime * result + ((topic == null) ? 0 : topic.hashCode());
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
	Subscribe other = (Subscribe) obj;
	if (groupId == null) {
	    if (other.groupId != null)
		return false;
	} else if (!groupId.equals(other.groupId))
	    return false;
	if (topic == null) {
	    if (other.topic != null)
		return false;
	} else if (!topic.equals(other.topic))
	    return false;
	return true;
    }

    // getter

    public String getTopic() {
	return topic;
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
