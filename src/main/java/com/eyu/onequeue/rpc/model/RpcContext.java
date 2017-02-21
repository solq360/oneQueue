package com.eyu.onequeue.rpc.model;

import com.eyu.onequeue.protocol.model.QRpc;
import com.eyu.onequeue.socket.model.QNode;

/**
 * @author solq
 * @version 2014-3-6 上午11:20:38
 */
public class RpcContext {
    private QNode[] nodes;
    private Long[] ids;
    private String[] addresses;

    public static RpcContext of(QNode... nodes) {
	RpcContext ret = new RpcContext();
	ret.nodes = nodes;
	return ret;
    }

    public static RpcContext of(Long... ids) {
	RpcContext ret = new RpcContext();
	ret.ids = ids;
	return ret;
    }

    public static RpcContext of(String... addresses) {
	RpcContext ret = new RpcContext();
	ret.addresses = addresses;
	return ret;
    }

    // get setter

    public void send(QRpc rpc) {
	/*
	 * for(long id : ids){ QNodeFactory.get(id); }
	 */
    }

}
