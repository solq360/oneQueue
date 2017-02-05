package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.store.model.QResult;

public interface IConsumeHandle {

    public void onSucceed(QResult qResult);
}
