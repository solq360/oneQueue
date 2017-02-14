package com.eyu.onequeue.socket.handler;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.util.QFactoryUtil;

/**
 * 
 * @author solq
 */
public class QClientHandler extends QCommonHandler {
    public QClientHandler() {
	QMConfig.getInstance().SERVER_MODEL = false;
	QFactoryUtil.putValue(QFactoryUtil.STORE_CLIENT_SERVICE, this.storeService);

	buildCommandHandler();
	buildClientHandler();
    }
}
