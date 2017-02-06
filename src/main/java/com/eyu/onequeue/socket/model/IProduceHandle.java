package com.eyu.onequeue.socket.model;

import java.util.List;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QProduce;

public interface IProduceHandle {

    public void onSucceed(QProduce produce);

    public void onError(String topic, List<QMessage<?,?>> qMessages, Exception e);
}
