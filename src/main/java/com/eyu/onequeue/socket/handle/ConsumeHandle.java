package com.eyu.onequeue.socket.handle;

import java.util.function.Consumer;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.socket.model.IConsumeHandle;

/***
 * 消费端业务逻辑
 * 
 * @author solq
 */
public class ConsumeHandle implements IConsumeHandle {

    public static ConsumeHandle of(Consumer<QMessage<?, ?>[]> action) {
	ConsumeHandle ret = new ConsumeHandle();
	ret.action = action;
	return ret;
    }

    private Consumer<QMessage<?, ?>[]> action;

    @Override
    public void onSucceed(QConsume qConsume) {
	qConsume.foreachMessageData(getAction());
    }

    void doSucceed(String topic, QConsume qResult) {

    }

    @Override
    public Consumer<QMessage<?, ?>[]> getAction() {
	return action;
    }
}
