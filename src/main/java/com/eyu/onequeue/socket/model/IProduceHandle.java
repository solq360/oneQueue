package com.eyu.onequeue.socket.model;

import java.util.List;

import com.eyu.onequeue.protocol.model.QMessage;

public interface IProduceHandle {

    public void onSucceed(String topic, List<QMessage<?,?>> qMessages);

    public void onError(String topic, List<QMessage<?,?>> qMessages, Exception e);
}
