package com.eyu.onequeue.callback.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/***
 * 通信返回结果包装
 * 
 * @author solq
 */
public class QResultWrapper<T> implements QResult<T> {
    private T result;

    public static <T> QResultWrapper<T> of(T value) {
	QResultWrapper<T> ret = new QResultWrapper<T>();
	ret.result = value;
	return ret;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
	return false;
    }

    @Override
    public boolean isCancelled() {
	return false;
    }

    @Override
    public boolean isDone() {
	return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
	return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
	return result;
    }

    @Override
    public boolean isError() {
	return false;
    }

    @Override
    public T getResult() {
	return result;
    }

    @Override
    public void setResult(T result) {
	this.result = result;
    }

}
