package com.eyu.onequeue.callback.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.protocol.model.IRecycle;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.util.PoolUtil;

/***
 * 响应异步消息 针对业务上的逻辑处理 {@link QNode#send(Object, IQCallback)}
 * 
 * @author solq
 */
public class QCallbackManager implements IRecycle {
    private final static Logger LOGGER = LoggerFactory.getLogger(QCallbackManager.class);

    private final static ScheduledExecutorService pool = PoolUtil.createScheduledPool(QMConfig.getInstance().POOL_CLEAR_MESSAGE_CORE, "message clear");

    private Map<Long, IQCallback<?>> messageRecoreds = Collections.synchronizedMap(new HashMap<>());

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void buildTask(long sn, IQCallback cb) {
	messageRecoreds.put(sn, cb);
	Future<?> future = pool.schedule(new Runnable() {
	    private long _sn = sn;

	    @Override
	    public void run() {
		IQCallback<?> _cb = messageRecoreds.remove(_sn);
		if (_cb == null) {
		    return;
		}
		try {
		    _cb.onReceiveError(QCode.MESSAGE_ERROR_TIMEOUT);
		} finally {
		    _cb.recycle();
		}

	    }
	}, QMConfig.getInstance().NETTY_MESSAGE_CALLBACK_CLEAR_INTERVAL, TimeUnit.MILLISECONDS);
	cb.setFuture(future);
    }

    public <T> IQCallback<T> doSend(QPacket sendPacket, IQCallback<T> cb) {
	if (cb == null) {
	    return null;
	}
	sendPacket.setStatus(QPacket.MASK_RESPONSE);
	cb.setSendPacket(sendPacket);
	buildTask(sendPacket.getSn(), cb);
	return cb;
    }

    public void doSendError(QPacket sendPacket, short code) {
	final long key = sendPacket.getSn();
	IQCallback<?> cb = messageRecoreds.remove(key);
	if (cb == null) {
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("发送失败 未找到回调 :" + key);
	    }
	    return;
	}
	try {
	    cb.onSendError(code);
	} finally {
	    cb.recycle();
	}
    }

    public void doReceiveSucceed(QPacket rePacket) {
	final long key = rePacket.getSn();
	IQCallback<?> cb = messageRecoreds.remove(key);
	if (cb == null) {
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("响应成功 未找到回调 :" + key);
	    }
	    return;
	}
	try {
	    short code = rePacket.toCode();
	    cb.setCode(code);
	    cb.onSucceed(code);
	} finally {
	    cb.recycle();
	}
    }

    public void doReceiveError(QPacket rePacket) {
	final long key = rePacket.getSn();
	IQCallback<?> cb = messageRecoreds.remove(key);
	if (cb == null) {
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("响应失败 未找到回调 :" + key);
	    }
	    return;
	}
	try {
	    short code = rePacket.toCode();
	    cb.setCode(code);
	    cb.onReceiveError(code);
	} finally {
	    cb.recycle();
	}
    }

    public int getMessageRecoredSize() {
	return messageRecoreds.size();
    }

    @Override
    public void recycle() {
	// 释放所有消息
	messageRecoreds.forEach((sn, cb) -> {
	    cb.recycle();
	});
	messageRecoreds.clear();
    }

}
