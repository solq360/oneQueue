package com.eyu.onequeue.socket.model;

import com.eyu.onequeue.callback.model.IQCallback;
import com.eyu.onequeue.protocol.model.IQService;

/**
 * @author solq
 */
public interface IQSocket extends IQService {
    public void sync();

    public QNode connect(String address, String id);

    public void send(QNode node, Object message);

    public <T> IQCallback<T> send(QNode node, Object message, IQCallback<T> cb);

    public void sendAll(Object message, IQCallback<?> cb);
}
