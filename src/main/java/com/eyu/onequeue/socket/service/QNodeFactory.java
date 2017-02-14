package com.eyu.onequeue.socket.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.eyu.onequeue.socket.model.QNode;

/**
 * @author solq
 **/
public class QNodeFactory {
    private static Map<Long, QNode> nodes = new HashMap<>();

    public static synchronized void registerNode(QNode node) {
	final long key = node.toId();
	QNode oldNode = nodes.remove(key);
	if (oldNode != null) {
	    node.replace(oldNode);
	}
	nodes.put(key, node);
    }

    public static synchronized QNode get(long id) {
	return nodes.get(id);
    }

    public static QNode get(String id) {
	long i = 0L;
	try {
	    i = Long.valueOf(id);
	} catch (Exception e) {
	    i = id.hashCode();
	}
	return get(i);
    }

    public static synchronized void removeNode(long id) {
	QNode node = nodes.remove(id);
	if (node == null) {
	    return;
	}
	node.recycle();
    }

    public static synchronized void closeAll() {
	for (QNode node : nodes.values()) {
	    node.recycle();
	}
	nodes.clear();
    }

    public static synchronized void foreach(Consumer<QNode> action) {
	nodes.forEach((k,v)->{
	    action.accept(v);
	});
    }

}
