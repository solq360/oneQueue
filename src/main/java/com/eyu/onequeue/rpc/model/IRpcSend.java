package com.eyu.onequeue.rpc.model;

import com.eyu.onequeue.callback.model.QResult;

/**
 * @author solq
 */
public interface IRpcSend {
    public <T> QResult<T> send(byte command, Object... args);
}
