package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.protocol.model.QConsume;

/***
 * 消息仓库接口，应用层使用
 * 
 * @author solq
 */
public interface IQStore extends IQService {

    public void save(Object... messages);

    public QConsume query(QQuery query);
    public QConsume queryForRaw(QQuery query);

    public void persist();

}
