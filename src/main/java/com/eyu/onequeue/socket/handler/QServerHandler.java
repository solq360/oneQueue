package com.eyu.onequeue.socket.handler;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.util.QFactoryUtil;

/**
 * 
 * @author solq
 */
public class QServerHandler extends QCommonHandler {
    public QServerHandler() {
	QMConfig.getInstance().SERVER_MODEL = true;
	QFactoryUtil.putValue(QFactoryUtil.STORE_CLIENT_SERVICE, this.storeService);
	buildCommandHandler();
	buildServerHandler();
    }
}
