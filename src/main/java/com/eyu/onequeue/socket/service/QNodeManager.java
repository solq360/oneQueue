package com.eyu.onequeue.socket.service;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.util.PoolUtil;

@Component
public class QNodeManager implements InitializingBean {
    private Map<Long, QNode> nodes = new HashMap<>();

    private static ScheduledExecutorService pool = PoolUtil.createScheduledPool(QMConfig.getInstance().POOL_CLEAR_MESSAGE_CORE, "nodeManager");

    public void registerNode(SocketAddress socketAddress, QNode node) {

    }

    public void removeNode(long id) {
	QNode node = nodes.remove(id);
	if (node == null) {
	    return;
	}
	node.release();
    }

    public void close() {
	pool.shutdownNow();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
