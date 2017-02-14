package com.eyu.onequeue.callback.model;

import java.util.concurrent.Future;

/***
 * 通信返回结果包装
 * 
 * @author solq
 */
public interface QResult<T> extends Future<T> {

    public T getResult();

    public void setResult(T result);
}
