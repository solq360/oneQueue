package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.protocol.model.QPacket;

/***
 * 请求端业务逻辑
 * @author solq
 * */
public interface IQRequestHandler {

    void onCompleted(QPacket packet);

    void onError(QPacket packet);
}
