package com.eyu.onequeue.store.model;

/***
 * 消息仓库接口，应用层使用
 * 
 * @author solq
 */
public interface IQMStore {

    public void save(Object... messages);

    public void save(byte[] bytes);

    public QResult query(QQuery query);

    public void persist();

    public void close();
}
