package com.eyu.onequeue.socket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.message.TestMessageObject;
import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.protocol.model.QSubscribe;
import com.eyu.onequeue.socket.coder.QChannelInitHandler;
import com.eyu.onequeue.socket.handler.QClientHandler;
import com.eyu.onequeue.socket.handler.QDispenseHandler;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.socket.service.QNettyClient;
import com.eyu.onequeue.store.model.IQStoreService;
import com.eyu.onequeue.util.QFactoryUtil;

public class TestQNettyClient {

    public static void main(String[] args) {
	QMConfig.getInstance().SERVER_MODEL = false;
	QDispenseHandler dispenseHandler = new QClientHandler();
	dispenseHandler.start();
	QNettyClient client = QNettyClient.of(QMConfig.getInstance().buildClientConfig(), new QChannelInitHandler(dispenseHandler));
	client.start();

	// 连接集群
	Set<String> address = QMConfig.getInstance().CLUSTER_LIST;
	String testAddress = null;
	for (String a : address) {
	    client.connect(a, null);
	    testAddress = a;
	}
	// 订阅消息
	Collection<QSubscribe> topics = QMConfig.getInstance().getTopics();
	client.sendAll(topics, new IQCallback<Void>() {
	    @Override
	    public void onSucceed(short code) {
		System.out.println("topic xxxxx");
	    }
	    @Override
	    public void onReceiveError(short code) {
 
	    }
	    @Override
	    public void onSendError(short code) {
 
	    }
	});
	//测试
	QNode node = client.connect(testAddress, null);
	// node.send(QPacket.of(QCode.SUCCEED), null);
	node.send(QPacket.of(ofProduce()), new IQCallback<Void>() {

	    @Override
	    public void onSucceed(short code) {
		System.out.println("onSucceed");
	    }

	    @Override
	    public void onReceiveError(short code) {
		System.out.println("onReceiveError");
		persistentMessage();
	    }

	    @Override
	    public void onSendError(short code) {
		System.out.println("onSendError");
		persistentMessage();
	    }

	    private void persistentMessage() {
		IQStoreService storeService = QFactoryUtil.getValue(QFactoryUtil.STORE_CLIENT_SERVICE);
		QProduce produce = this.sendPacket.toProduce();
		storeService.save(produce.getT(), produce.getB());
	    }
	});

	client.sync();
    }

    static QProduce ofProduce() {
	int count = 2000;
	TestMessageObject obj = TestMessageObject.ofBig(200);
	List<QMessage<Long, TestMessageObject>> tmp = new ArrayList<>();
	for (int i = 0; i < count; i++) {
	    tmp.add(QMessage.of(1L, obj));
	}
	QProduce qm = QProduce.of("test", tmp.toArray(new QMessage[count]));
	return qm;
    }
}
