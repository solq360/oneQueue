package com.eyu.onequeue.proxy.model.rpc;

import java.lang.reflect.Type;

/***
 * @author solq
 */
public class RpcParam {
    private Type type;

    public Object encode(Object obj) {
	return null;
    }

    public Object decode(Object obj) {
	return null;
    }

    public static RpcParam of(Type type) {
	RpcParam ret = new RpcParam();
	ret.type = type;
	return ret;
    }
}
