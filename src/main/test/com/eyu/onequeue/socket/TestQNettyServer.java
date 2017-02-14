package com.eyu.onequeue.socket;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.socket.coder.QChannelInitHandler;
import com.eyu.onequeue.socket.handler.QDispenseHandler;
import com.eyu.onequeue.socket.handler.QServerHandler;
import com.eyu.onequeue.socket.service.QNettyServer;

public class TestQNettyServer {

    public static void main(String[] args) {

	QDispenseHandler dispenseHandler = new QServerHandler();
	dispenseHandler.start();
	QNettyServer server = QNettyServer.of(QMConfig.getInstance().buildServerConfig(), QMConfig.getInstance().buildClientConfig(), new QChannelInitHandler(dispenseHandler));
	server.start();
	server.connect("127.0.0.1:8080", "sss");
	server.connect("127.0.0.1:8080", "sss");
	server.sync();
    }
}
