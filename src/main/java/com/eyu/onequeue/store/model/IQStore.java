package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QProduce;

/***
 * 消息仓库接口，应用层使用
 * 
 * @author solq
 */
public interface IQStore extends IQService {

    public void save(Object... messages);

    public void save(byte[] bytes);

    public QConsume query(QQuery query);

    public QProduce queryForProduce(QQuery query);

    public void persist();

}
