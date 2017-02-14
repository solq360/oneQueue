package com.eyu.onequeue;

import com.eyu.onequeue.socket.coder.QChannelInitHandler;
import com.eyu.onequeue.socket.handler.QClientHandler;
import com.eyu.onequeue.socket.handler.QDispenseHandler;
import com.eyu.onequeue.socket.service.QNettyClient;

/**
 * @author solq
 **/
public class ClientMain {

    public static void main(String[] args) {
	QDispenseHandler dispenseHandler = new QClientHandler();
	dispenseHandler.start();
	QNettyClient client = QNettyClient.of(QMConfig.getInstance().buildClientConfig(), new QChannelInitHandler(dispenseHandler));
	client.start();
	client.sync();
    }
}
