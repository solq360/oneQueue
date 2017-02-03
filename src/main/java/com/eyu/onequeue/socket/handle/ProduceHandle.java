package com.eyu.onequeue.socket.handle;

import java.util.List;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.socket.model.IProduceHandle;

/***
 * 生产端业务处理
 * @author solq
 * */
public class ProduceHandle implements IProduceHandle{

	@Override
	public void onSucceed(String topic, List<QMessage<?, ?>> qMessages) {
 		//什么也不做
	}

	@Override
	public void onError(String topic, List<QMessage<?, ?>> qMessages, Exception e) {
		//持化久重做
		
	}

}
