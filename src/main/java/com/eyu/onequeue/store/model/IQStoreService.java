package com.eyu.onequeue.store.model;

import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QProduce;

/**
 * @author solq
 **/
public interface IQStoreService extends IQService, IStoreMBean {
    
    public void save(String topic, Object... messages);

    public QConsume query(QQuery query);
    public QProduce queryForProduce(QQuery query);

    public void persist();

}
