package com.eyu.onequeue.callback.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QSocketException;
import com.eyu.onequeue.protocol.model.IRecycle;
import com.eyu.onequeue.protocol.model.QPacket;

/**
 * @author solq
 **/
public abstract class IQCallback<T> implements IRecycle, QResult<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(IQCallback.class);

    /** 线程等待间隔时间 */
    private final static int THREAD_WAIT = 120;
    /***
     * 发送数据包
     */
    protected QPacket sendPacket;
    /***
     * 发送时间，目前只做记录使用
     */
    protected long sendTime;

    /***
     * 控制超时响应任务
     */
    protected Future<?> future;
    /***
     * 返回内容
     */
    protected T result;

    protected Short code = null;

    abstract public void onSucceed(short code);

    // 默认什么也不用做
    public void onSendError(short code) {
	if (LOGGER.isWarnEnabled()) {
	    LOGGER.warn("onSendError : {)", code);
	}
    }

    // 默认什么也不用做
    public void onReceiveError(short code) {
	if (LOGGER.isWarnEnabled()) {
	    LOGGER.warn("onReceiveError : {)", code);
	}
    }

    // get setter
    public void setSendPacket(QPacket sendPacket) {
	this.sendPacket = sendPacket;
	sendTime = System.currentTimeMillis();
    }

    // 实现 Future

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
	return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
	return future.isCancelled() || code == null;
    }

    @Override
    public boolean isDone() {
	return future.isDone() || code != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
	try {
	    return get(QMConfig.getInstance().NETTY_MESSAGE_REQUEST_TIMEOUT, TimeUnit.MICROSECONDS);
	} catch (TimeoutException e) {
	    throw new QSocketException(QCode.SOCKET_ERROR_REQUEST_TIMEOUT);
	}
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
	if (isDone() || isCancelled()) {
	    return result;
	}
	long waitTime = unit.toMillis(timeout);
	while (waitTime > 0) {
	    long step = waitTime > THREAD_WAIT ? THREAD_WAIT : waitTime;
	    synchronized (this) {
		wait(step);
	    }
	    if (isDone() || isCancelled()) {
		return result;
	    }
	    waitTime -= THREAD_WAIT;
	}
	throw new QSocketException(QCode.SOCKET_ERROR_REQUEST_TIMEOUT);
    }

    @Override
    public T getResult() {
	T ret = null;
	try {
	    ret = get();
	} catch (Exception e) {
	    throw new QSocketException(QCode.SOCKET_ERROR_REQUEST_TIMEOUT, null, e);
	}
	return ret;
    }

    @Override
    public boolean isError() {
	try {
	    getResult();
	} catch (Exception e) {
	    return true;
	}
	if (code == null) {
	    return true;
	}

	return QCode.SUCCEED == code;
    }

    @Override
    public void recycle() {
	if (code == null) {
	    code = QCode.MESSAGE_ERROR_RECYCLE;
	}
	if (sendPacket != null) {
	    sendPacket.recycle();
	    sendPacket = null;
	}
	if (future != null && !future.isDone()) {
	    try {
		future.cancel(true);
	    } catch (Exception e) {
	    }
	}
    }

    // get setter

    public long getSendTime() {
	return sendTime;
    }

    public long getSn() {
	return sendPacket.getSn();
    }

    public void setFuture(Future<?> future) {
	this.future = future;
    }

    public void setResult(T result) {
	this.result = result;
    }

    public void setCode(Short code) {
	this.code = code;
    }

}
