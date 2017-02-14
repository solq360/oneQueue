package com.eyu.onequeue.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author solq
 **/
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

    public static void foreachMethods(Class<?> clazz, Consumer<Method> action) {

	Method[] methods = clazz.getDeclaredMethods();
	for (Method method : methods) {
	    action.accept(method);
	}
	if (clazz.getSuperclass() != null) {
	    foreachMethods(clazz.getSuperclass(), action);
	} else if (clazz.isInterface()) {
	    for (Class<?> superIfc : clazz.getInterfaces()) {
		foreachMethods(superIfc, action);
	    }
	}
    }

    public static void foreachFields(Class<?> clz, Consumer<Field> action) {
	try {
	    do {
		Field[] fields = clz.getDeclaredFields();
		for (Field f : fields) {
		    f.setAccessible(true);
		    action.accept(f);
		}
		clz = clz.getSuperclass();
	    } while (clz != null && clz != Object.class);
	} catch (Exception e1) {
	    e1.printStackTrace();
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
