package com.eyu.onequeue.rpc.service;

import com.eyu.onequeue.proxy.service.JavassistProxy;
import com.eyu.onequeue.rpc.model.IRpcContext;
import com.eyu.onequeue.rpc.model.RpcContext;
import com.eyu.onequeue.socket.model.QNode;

/****
 * 
 * @author solq
 */
public abstract class QRpcFactory {
    private final static QRpcEnhanceService enhanceService = QRpcEnhanceService.getFactory();

    public static <T> T loadProxy(Class<T> target, QNode... nodes) {
	JavassistProxy.getDefault().register(target, enhanceService);
	T ret = JavassistProxy.getDefault().transform(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(nodes));
	return ret;
    }

    public static <T> T loadProxy(Class<T> target, Long... ids) {
	JavassistProxy.getDefault().register(target, enhanceService);
	T ret = JavassistProxy.getDefault().transform(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(ids));
	return ret;
    }

    public static void registerInvokeService(Object target) {
	enhanceService.registerInvokeService(target);
    }
}
