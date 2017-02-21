package com.eyu.onequeue.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.rpc.model.IRpcContext;
import com.eyu.onequeue.rpc.model.RpcContext;
import com.eyu.onequeue.rpc.service.QRpcFactory;
import com.eyu.onequeue.socket.model.QNode;

/****
 * 
 * @author solq
 */
@SuppressWarnings("unchecked")
public abstract class QRpcFactory2 {

    public static <T> T loadProxy(Class<T> target, QNode... nodes) {
	T ret = loadProxy0(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(nodes));
	return ret;
    }

    public static <T> T loadProxy(Class<T> target, Long... ids) {
	T ret = loadProxy0(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(ids));
	return ret;
    }

    private static <T> T loadProxy0(Class<T> target) {
	T ret = (T) Proxy.newProxyInstance(QRpcFactory.class.getClassLoader(), new Class[] { target, IRpcContext.class }, new InvocationHandler() {
	    QModel modelAnno = target.getAnnotation(QModel.class);
	    private short model = modelAnno.value();
	    private RpcContext ctx;

	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("setContext")) {
		    this.ctx = (RpcContext) args[0];
		    System.out.println("setContext");
		    return null;
		}
		QCommond commondAnno = method.getAnnotation(QCommond.class);
		// do send

		return null;
	    }
	});
	return ret;
    }

    public static void main(String[] args) {
	TestRpcObject obj = QRpcFactory2.loadProxy(TestRpcObject.class, 123L);
    }
    // private static <T> T loadProxy0(Class<T> target) {
    // T ret = (T) PROXY_OBJECT.get(target);
    // if (ret != null) {
    // return ret;
    // }
    // // dobule check
    // synchronized (target) {
    // ret = (T) PROXY_OBJECT.get(target);
    // if (ret != null) {
    // return ret;
    // }
    // ret = (T) Proxy.newProxyInstance(QRpcFactory.class.getClassLoader(), new
    // Class[] { target, IRpcContext.class }, new InvocationHandler() {
    // QModel modelAnno = target.getAnnotation(QModel.class);
    // private short model = modelAnno.value();
    // private RpcContext ctx;
    //
    // @Override
    // public Object invoke(Object proxy, Method method, Object[] args) throws
    // Throwable {
    // if (method.getName().equals("setContext")) {
    // this.ctx = (RpcContext) args[0];
    // System.out.println("setContext");
    // return null;
    // }
    // QCommond commondAnno = method.getAnnotation(QCommond.class);
    // // do send
    //
    // return null;
    // }
    // });
    // // put proxy instantiate
    // PROXY_OBJECT.put(target, ret);
    // }
    // return ret;
    // }
}
