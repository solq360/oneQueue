package com.eyu.onequeue.socket.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/***
 * 通信返回结果包装
 * 
 * @author solq
 */
public class QResult<T> implements Future<T> {
    private T result;
    private boolean completed = false;

    public void onSucceed(T result) {
	this.result = result;
	completed = true;
    }

    // getter
    public boolean isCompleted() {
	return completed;
    }

    public T getResult() {
	return result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isCancelled() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isDone() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
 	return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
	// TODO Auto-generated method stub
	return result;
    }

}
