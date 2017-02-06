package com.eyu.onequeue.socket.handler;

import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.socket.model.IQResponseHandler;

/***
 * 响应消费业务逻辑
 * 
 * @author solq
 */
public class ConsumeResponseHandler implements IQResponseHandler<QConsume> {

    @Override
    public void onReceive(QPacket qpacket, QConsume qConsume) {

    }

}
