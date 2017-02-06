package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.QConsume;

public interface IQMStoreService {
	public void save(String topic, Object... messages);

	public QConsume query(QQuery query);

	public void persist();

	public void close();

}
