package com.eyu.onequeue.facade;

import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;

public interface IQMServerFacade {
	public void save(String topic, Object... messages);

	public QResult query(QQuery query);

	public void persist();

	public void close();

	public void registerNode(String topic, String groupId, QNode node);
	public void removeNode(String topic, String groupId, QNode node);
	public void removeNode(QNode node);
}
