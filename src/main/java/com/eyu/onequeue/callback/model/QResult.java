package com.eyu.onequeue.callback.model;

import java.util.concurrent.Future;

/***
 * 通信返回结果包装
 * 
 * @author solq
 */
public interface QResult<T> extends Future<T> {

    /**
     * 是否出错
     * */
    public boolean isError();
    /**
     * 获取返回结果
     * */   
    public T getResult();
    /**
     * 设置结果
     * */  
    public void setResult(T result);
}
