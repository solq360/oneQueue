package com.eyu.onequeue.rpc.model;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.rpc.anno.QRpcParam;

/***
 * @author solq
 */
public class RpcMethod {
    private Map<Integer, RpcParam> params = new HashMap<>();
    private Method method;

    public Map<Integer, RpcParam> getParams() {
	return params;
    }

    public Method getMethod() {
	return method;
    }

    public static RpcMethod of(Method method) {
	method.setAccessible(true);
	RpcMethod ret = new RpcMethod();
	ret.method = method;
	final Type[] ptypes = method.getGenericParameterTypes();
	for (int i = 0; i < ptypes.length; i++) {
	    QRpcParam anno = JavassistHepler.getParamAnno(method, QRpcParam.class, i);
	    ret.params.put(i, RpcParam.of(ptypes[i], anno));
	}
	return ret;
    }

}
