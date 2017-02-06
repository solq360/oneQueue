package com.eyu.onequeue.exception;

import org.slf4j.helpers.FormattingTuple;

import com.eyu.onequeue.util.ReflectUtil;

/**
 * 
 * @author solq
 */
public class QException extends RuntimeException {

    private static final long serialVersionUID = -3110633035340065406L;

    private int code;

    public int getCode() {
	return code;
    }

    public static RuntimeException of(Class<? extends QException> clz, int code) {
	RuntimeException ret = of2(clz, code, null, null);
	return ret;
    }

    public static RuntimeException of(Class<? extends QException> clz, int code, String message) {
	RuntimeException ret = of2(clz, code, message, null);
	return ret;
    }

    public static RuntimeException of2(Class<? extends QException> clz, int code, String message, Throwable cause) {
	RuntimeException ret = null;
	try {
	    ret = clz.newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	ReflectUtil.invokeExceptionMessage(ret, message, cause);
	return ret;
    }
    
    public static RuntimeException of(Class<? extends QException> clz, int code, FormattingTuple message, Throwable cause) {
	RuntimeException ret = of2(clz, code, message.getMessage(), cause);
	return ret;
    }
    
}
