package com.eyu.onequeue.socket.model;

import java.util.function.Consumer;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QConsume;

public interface IConsumeHandle {

    public void onSucceed(QConsume qConsume);
    
    public Consumer<QMessage<?, ?>[]> getAction();
}
