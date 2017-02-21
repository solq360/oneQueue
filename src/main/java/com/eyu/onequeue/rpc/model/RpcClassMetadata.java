package com.eyu.onequeue.rpc.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils.Null;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QEnhanceException;
import com.eyu.onequeue.proxy.model.ClassMetadata;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.util.SerialUtil;

/***
 * @author solq
 */
public class RpcClassMetadata extends ClassMetadata {

    private Map<Byte, RpcMethod> methods = new HashMap<>();
    private QModel model;
    private Object target;

    @Override
    public void record(Method method) {
	if (!this.enhanceService.matches(method)) {
	    return;
	}
	QCommond commond = method.getAnnotation(QCommond.class);
	methods.put(commond.value(), RpcMethod.of(method));
    }

    public byte[] encode(byte methodCommand, int paramIndex, Object object) {
	if (object == null) {
	    return null;
	}
	RpcMethod method = methods.get(methodCommand);
	return (byte[]) method.getParams().get(paramIndex).encode(object);
    }

    public Object decode(byte methodCommand, int paramIndex, byte[] bytes) {
	return getAndTryRpcParam(methodCommand, paramIndex).decode(bytes);
    }

    public Object invoke(byte methodCommand, Object[] args) {
	RpcMethod method = methods.get(methodCommand);
	try {
	    return method.getMethod().invoke(target, args);
	} catch (IllegalArgumentException e) {
	    String message = " 参数类型不匹配 : " + method.getMethod().toGenericString() + " args : ";
	    List<Class<?>> types = new ArrayList<>(args.length);
	    for (Object obj : args) {
		if (obj == null) {
		    types.add(Null.class);
		}else{
		    types.add(obj.getClass());
		}
		
	    }
	    message += SerialUtil.writeValueAsString(types);
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_INVOKE, message, e);

	} catch (Exception e) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_INVOKE, null, e);
	}
    }

    public boolean checkRequired(byte methodCommand, int paramIndex) {
	return getAndTryRpcParam(methodCommand, paramIndex).checkRequired();
    }

    public RpcParam getAndTryRpcParam(byte methodCommand, int paramIndex) {
	RpcMethod method = methods.get(methodCommand);
	RpcParam ret = method.getParams().get(paramIndex);
	if (ret == null) {
	    throw new QEnhanceException(QCode.ENHANCE_ERROR_RPC_PARAM_ILLEGITMACY, "clz:" + clz + " 参数非法  : " + methodCommand + "  paramIndex : " + paramIndex);
	}
	return ret;
    }

    public static RpcClassMetadata of(Class<?> clz) {
	RpcClassMetadata ret = new RpcClassMetadata();
	ret.model = clz.getAnnotation(QModel.class);
	ret.clz = clz;
	return ret;
    }

    // getter
    public Map<Byte, RpcMethod> getMethods() {
	return methods;
    }

    public QModel getModel() {
	return model;
    }

    public Object getTarget() {
	return target;
    }

    public void setTarget(Object target) {
	this.target = target;
    }

}
