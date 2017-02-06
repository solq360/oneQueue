package com.eyu.onequeue.callback.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.protocol.model.IRelease;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.util.PoolUtil;

/***
 * 响应异步消息
 * 
 * @author solq
 */
public class QCallbackManager implements IRelease {
    private static ScheduledExecutorService pool = PoolUtil.createScheduledPool(QMConfig.getInstance().POOL_CLEAR_MESSAGE_CORE, "message clear");

    private Map<Long, IQCallback> messageRecoreds = Collections.synchronizedMap(new HashMap<>());

    private void buildTask(long sn, IQCallback cb) {
	messageRecoreds.put(sn, cb);
	
	//是否监听响应消息
	
	Future<?> future = pool.schedule(new Runnable() {
	    private long _sn = sn;

	    @Override
	    public void run() {
		IQCallback _cb = messageRecoreds.remove(_sn);
		if (_cb == null) {
		    return;
		}
		long now = System.currentTimeMillis();
		if ((now - _cb.getSendTime()) < QMConfig.getInstance().MESSAGE_CALLBACK_CLEAR_INTERVAL) {
		    buildTask(_sn, _cb);
		    return;
		}
		try {
		    cb.onReceiveError(QCode.MESSAGE_ERROR_TIMEOUT);
		} finally {
		    cb.release();
		}

	    }
	}, QMConfig.getInstance().MESSAGE_CALLBACK_CLEAR_INTERVAL, TimeUnit.MILLISECONDS);
	cb.setFuture(future);
    }

    public void doRequest(QPacket sendPacket, IQCallback cb) {
	cb.setSendPacket(sendPacket);
	buildTask(sendPacket.getSn(), cb);
    }

    public void doSucceedResponse(QPacket rePacket) {
	final long key = rePacket.getSn();
	IQCallback cb = messageRecoreds.remove(key);
	if (cb == null) {
	    return;
	}
	try {
	    cb.onSucceed(rePacket);
	} finally {
	    cb.release();
	}
    }

    public void doErrorReceiveResponse(QPacket rePacket) {
	final long key = rePacket.getSn();
	IQCallback cb = messageRecoreds.remove(key);
	if (cb == null) {
	    return;
	}
	try {
	    cb.onReceiveError(rePacket.toCode());
	} finally {
	    cb.release();
	}
    }

    public void doErrorSendResponse(QPacket rePacket) {
	final long key = rePacket.getSn();
	IQCallback cb = messageRecoreds.remove(key);
	if (cb == null) {
	    return;
	}
	try {
	    cb.onSendError();
	} finally {
	    cb.release();
	}
    }

    @Override
    public void release() {
	//释放所有消息
	messageRecoreds.forEach((sn, cb) -> {
	    cb.release();
	});
	messageRecoreds.clear();
    }

}
