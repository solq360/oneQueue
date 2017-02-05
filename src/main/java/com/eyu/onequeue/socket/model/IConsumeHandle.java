package com.eyu.onequeue.socket.model;

import java.util.function.Consumer;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.store.model.QResult;

public interface IConsumeHandle {

    public void onSucceed(QResult qResult);
    
    public Consumer<QMessage<?, ?>[]> getAction();
}
