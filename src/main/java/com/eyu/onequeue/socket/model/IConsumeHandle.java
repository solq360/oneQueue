package com.eyu.onequeue.socket.model;

public interface IConsumeHandle {

	public void onSucceed(String topic,  byte[] bytes);
}
