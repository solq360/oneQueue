package com.eyu.onequeue.callback.model;

import java.util.concurrent.Future;

import com.eyu.onequeue.protocol.model.IRelease;
import com.eyu.onequeue.protocol.model.QPacket;

public abstract class IQCallback implements IRelease {

    private QPacket sendPacket;
    private long sendTime;
    private Future<?> future;

    // get setter
    public void setSendPacket(QPacket sendPacket) {
	this.sendPacket = sendPacket;
	sendTime = System.currentTimeMillis();
    }

    public long getSendTime() {
	return sendTime;
    }

    public long getSn() {
	if (sendPacket == null) {
	    return -1;
	}
	return sendPacket.getSn();
    }

    public void setFuture(Future<?> future) {
	this.future = future;
    }

    @Override
    public void release() {
	if (sendPacket != null) {
	    sendPacket.release();
	}
	sendPacket = null;
	if (future != null && !future.isDone()) {
	    try {
		future.cancel(true);
	    } catch (Exception e) {
	    }
	}
    }

    abstract public void onSucceed(QPacket rePacket);

    abstract public void onSendError();

    abstract public void onReceiveError(short code);
}
