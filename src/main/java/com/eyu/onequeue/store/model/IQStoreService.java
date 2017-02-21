package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.protocol.model.QConsume;

/**
 * @author solq
 **/
public interface IQStoreService extends IQService, IStoreMBean {

    public void save(String topic,Object... messages);

    public QConsume query(QQuery query);

    public void persist();

}
