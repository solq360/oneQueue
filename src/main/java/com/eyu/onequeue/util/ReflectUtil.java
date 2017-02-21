package com.eyu.onequeue.util;

import java.lang.annotation.Annotation;
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
	for (Class<?> superIfc : clazz.getInterfaces()) {
	    foreachMethods(superIfc, action);
	}

	if (clazz.getSuperclass() != null) {
	    foreachMethods(clazz.getSuperclass(), action);
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

    public static <A extends Annotation> A getAnno(Class<?> clz, Class<A> annotationClass) {
	A anno = clz.getAnnotation(annotationClass);
	FLAG: while (anno == null) {
	    for (Class<?> i : clz.getInterfaces()) {
		anno = i.getAnnotation(annotationClass);
		if (anno != null) {
		    break FLAG;
		}
	    }
	    clz = clz.getSuperclass();
	    if (clz == null) {
		break;
	    }
	    anno = clz.getAnnotation(annotationClass);
	}
	return anno;
    }

    public static Class<?> getInterfaceForAnno(Class<?> clz, Class<? extends Annotation> annotationClass) {
	Class<?> ret = null;
	Annotation anno = clz.getAnnotation(annotationClass);
	if (anno != null && clz.isInterface()) {
	    return clz;
	}
	FLAG: while (anno == null) {
	    for (Class<?> i : clz.getInterfaces()) {
		anno = i.getAnnotation(annotationClass);
		if (anno != null && i.isInterface()) {
		    ret = i;
		    break FLAG;
		}
	    }
	    clz = clz.getSuperclass();
	    if (clz == null) {
		break;
	    }
	    anno = clz.getAnnotation(annotationClass);
	}
	return ret;
    }

    public static Method getMethod(Class<?> target, String name) {
	Method[] methods = target.getDeclaredMethods();
	for (Method method : methods) {
	    if (method.getName().equals(name)) {
		return method;
	    }
	}
	if (target.getSuperclass() != null) {
	    return getMethod(target.getSuperclass(), name);
	}
	if (target.isInterface()) {
	    for (Class<?> superIfc : target.getInterfaces()) {
		return getMethod(superIfc, name);
	    }
	}

	return null;
    }
}
