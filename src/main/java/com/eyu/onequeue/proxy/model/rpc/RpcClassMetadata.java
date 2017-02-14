package com.eyu.onequeue.proxy.model.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.eyu.onequeue.proxy.model.ClassMetadata;
import com.eyu.onequeue.rpc.anno.QCommond;

/***
 * @author solq
 */
public class RpcClassMetadata extends ClassMetadata {

    private Map<Byte, RpcMethod> methods = new HashMap<>();

    @Override
    public void record(Method method) {
	if (!this.enhanceService.matches(method)) {
	    return;
	}
	QCommond commond = method.getAnnotation(QCommond.class);
	methods.put(commond.value(), RpcMethod.of(method));
    }

    public Map<Byte, RpcMethod> getMethods() {
	return methods;
    }

}
