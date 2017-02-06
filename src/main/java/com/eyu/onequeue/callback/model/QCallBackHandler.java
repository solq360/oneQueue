package com.eyu.onequeue.callback.model;

import com.eyu.onequeue.protocol.model.QPacket;

public class QCallBackHandler extends IQCallback {

    @Override
    public void onSucceed(QPacket rePacket) {

    }

    @Override
    public void onSendError() {
 	
    }

    @Override
    public void onReceiveError(short code) {
	
    }
 
}
