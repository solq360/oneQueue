package com.eyu.onequeue.socket.handle;

import java.util.List;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.socket.model.IConsumeHandle;
import com.eyu.onequeue.store.model.QResult;

/***
 * 消费端业务逻辑
 * 
 * @author solq
 */
public class ConsumeHandle implements IConsumeHandle {

    @Override
    public void onSucceed(QResult qResult) {
	doSucceed(qResult.getTopic(), qResult.toMessageData());
    }

    public void doSucceed(String topic, List<QMessage<?, ?>> qMessages) {
	System.out.println(qMessages.size());
    }
}
