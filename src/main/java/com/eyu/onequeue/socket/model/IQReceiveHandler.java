package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.protocol.model.QPacket;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * 响应端 业务逻辑
 * 
 * @author solq
 **/
public interface IQReceiveHandler<T> {

    void onReceive(ChannelHandlerContext ctx,QPacket packet, T body);

}
