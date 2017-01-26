package com.eyu.onequeue.store.model;

public interface IQMStoreService {
    public void save(String topic, Object... messages);
    public QResult query(QQuery query);

    public void persist();
    public void close();
}
