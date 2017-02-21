package com.eyu.onequeue.rpc.model;

import java.lang.reflect.Type;

import com.eyu.onequeue.rpc.anno.QRpcParam;
import com.eyu.onequeue.rpc.codec.TypeCode;

/***
 * 时间关系，目前不支持嵌套，最优处理
 * 
 * @author solq
 */
public class RpcParam {
    private Type type;
    private byte typeCode;
    private QRpcParam paramAnno;

    public Object encode(Object obj) {
	byte[] ret = TypeCode.ACTIONS.get(typeCode).encode(obj);
	return ret;
    }

    public Object decode(Object obj) {
	byte[] bytes = (byte[]) obj;
	Object ret = TypeCode.ACTIONS.get(typeCode).decode(type, bytes);
	return ret;
    }

    public boolean checkRequired() {
	return paramAnno != null && paramAnno.required();
    }

    public static RpcParam of(Type type, QRpcParam paramAnno) {
	RpcParam ret = new RpcParam();
	ret.type = type;
	ret.paramAnno = paramAnno;
	ret.typeCode = TypeCode.getCode(type);
	return ret;
    }
    // getter

    public Type getType() {
	return type;
    }

    public byte getTypeCode() {
	return typeCode;
    }

    public QRpcParam getParamAnno() {
	return paramAnno;
    }

}
