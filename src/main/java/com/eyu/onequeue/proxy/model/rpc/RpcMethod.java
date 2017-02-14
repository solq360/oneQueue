package com.eyu.onequeue.proxy.model.rpc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/***
 * @author solq
 */
public class RpcMethod {
    private Map<Integer, RpcParam> methods = new HashMap<>();
    private Method method;

    public Map<Integer, RpcParam> getMethods() {
        return methods;
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
	    ret.methods.put(i, RpcParam.of(ptypes[i]));
	}
	return ret;
    }

}
