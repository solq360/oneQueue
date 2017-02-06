package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.QConsume;

/***
 * 消息仓库接口，应用层使用
 * 
 * @author solq
 */
public interface IQMStore {

    public void save(Object... messages);

    public void save(byte[] bytes);

    public QConsume query(QQuery query);
   // public QResult queryForRaw(QQuery query);

    public void persist();

    public void close();
}
