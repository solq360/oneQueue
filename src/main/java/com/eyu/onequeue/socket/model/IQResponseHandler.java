package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.protocol.model.QPacket;

/**
 * 
 * 响应端 业务逻辑
 * 
 * @author solq
 **/
public interface IQResponseHandler<T> {

    void onReceive(QPacket packet, T body);

}
