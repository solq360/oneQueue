package com.eyu.onequeue;

import com.eyu.onequeue.socket.coder.QChannelInitHandler;
import com.eyu.onequeue.socket.handler.QDispenseHandler;
import com.eyu.onequeue.socket.handler.QServerHandler;
import com.eyu.onequeue.socket.service.QNettyServer;

/**
 * @author solq
 **/
public class ServerMain {

    public static void main(String[] args) {
	QDispenseHandler dispenseHandler = new QServerHandler();
	dispenseHandler.start();
	QNettyServer server = QNettyServer.of(QMConfig.getInstance().buildServerConfig(), QMConfig.getInstance().buildClientConfig(), new QChannelInitHandler(dispenseHandler));
	server.start();
	server.sync();
    }
}
