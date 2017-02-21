package com.eyu.onequeue.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.reflect.anno.Des;

/**
 * @author solq
 **/
public abstract class QCodeUtil {
    private static final Map<Integer, String> mapDes = new HashMap<>();

    static {
	ReflectUtil.foreachFields(QCode.class, (f) -> {
	    if ((f.getModifiers() & Modifier.STATIC) == 0) {
		return;
	    }
	    if (!TypeUtils.isAssignable(f.getType(), Short.class)) {
		return;
	    }
	    Des anno = f.getAnnotation(Des.class);
	    if (anno == null) {
		return;
	    }
	    f.setAccessible(true);
	    try {
		short value = (short) f.get(null);
		mapDes.put(Integer.valueOf(value), anno.value());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	});

    }

    public static void println(int code) {
	String des = mapDes.get(code);
	System.out.println("des : " + des);
    }

    public static String getDes(int code) {
	String des = mapDes.get(code);
	return des;
    }
}
