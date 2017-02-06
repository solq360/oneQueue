package com.eyu.onqueue.socket;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.socket.coder.QMessageHandler;
import com.eyu.onequeue.socket.handler.QChannelInitHandler;
import com.eyu.onequeue.socket.handler.QDispenseHandler;
import com.eyu.onequeue.socket.service.QNettyServer;

import io.netty.channel.ChannelHandler;

public class TestQNettyServer {

    public static void main(String[] args) {
	QDispenseHandler dispenseHandler = QDispenseHandler.of(null, null, null, null);
	ChannelHandler handler = new QMessageHandler(dispenseHandler);
	QNettyServer server = QNettyServer.of(QMConfig.getInstance().buildServerConfig(), new QChannelInitHandler(handler));
	server.start();
	//server.close();
    }
}
