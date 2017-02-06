package com.eyu.onequeue.util;

import java.lang.reflect.Field;

public class ReflectUtil {

    private static Field throwableDetailMessage = null;
    private static Field throwableCause = null;

    static {
	try {
	    throwableDetailMessage = Throwable.class.getDeclaredField("detailMessage");
	    throwableDetailMessage.setAccessible(true);

	    throwableCause = Throwable.class.getDeclaredField("cause");
	    throwableCause.setAccessible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public static void invokeExceptionMessage(Throwable throwable, String message, Throwable cause) {
	try {
	    throwable.fillInStackTrace();
	    if (message != null) {
		throwableDetailMessage.set(throwable, message);
	    }
	    if (throwableCause != null) {
		throwableCause.set(throwable, cause);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
