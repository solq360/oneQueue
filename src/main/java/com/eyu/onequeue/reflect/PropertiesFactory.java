package com.eyu.onequeue.reflect;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.eyu.onequeue.reflect.anno.FieldValue;
import com.eyu.onequeue.util.SerialUtil;

/**
 * @author solq
 */
public class PropertiesFactory {
    static final ScriptEngine se = new ScriptEngineManager().getEngineByName("JavaScript");

    public static <T> T initField(Class<T> clz, String file) {
	T ret = null;
	try {
	    Properties pro = getProperties(file);
	    ret = clz.newInstance();
	    Field[] fields = clz.getDeclaredFields();
	    Set<Method> methods = new HashSet<>();
	    Collections.addAll(methods, clz.getDeclaredMethods());
	    // Field modifiersField = Field.class.getDeclaredField("modifiers");
	    // modifiersField.setAccessible(true);
	    for (Field f : fields) {
		FieldValue anno = f.getAnnotation(FieldValue.class);
		if (anno == null) {
		    continue;
		}

		String fileName = anno.value();
		String tmp = (String) pro.get(fileName);
		if (tmp == null) {
		    continue;
		}
		Object value = tmp;
		Type type = f.getGenericType();
		if (type.equals(Integer.TYPE)) {
		    value = se.eval(tmp);
		} else if (type.equals(Double.TYPE)) {
		    value = se.eval(tmp);
		} else if (type.equals(Float.TYPE)) {
		    value = se.eval(tmp);
		} else if (type.equals(Long.TYPE)) {
		    value = se.eval(tmp);
		} else if (type.equals(Short.TYPE)) {
		    value = ((Integer) se.eval(tmp)).shortValue();
		} else if (type.equals(Byte.TYPE)) {
		    value = ((Integer) se.eval(tmp)).byteValue();
		} else if (type.equals(Boolean.TYPE)) {
		    value = se.eval(tmp);
		} else if (TypeUtils.isAssignable(type, Map.class)) {
		    value = SerialUtil.readValue(tmp, type);
		}else if (TypeUtils.isAssignable(type, Collection.class)) {
		    value = SerialUtil.readValue(tmp, type);
		}else if (TypeUtils.isAssignable(type, Class.class)) {
		    value = Class.forName(tmp);
		}else if (TypeUtils.isArrayType(type)) {
		    value = SerialUtil.readArray(tmp, type);
		}
		 
		String callMethodName = "set" + f.getName();
		boolean flag = false;
		try {
		    for (Method m : methods) {
			if (m.getName().equals(callMethodName)) {
			    m.setAccessible(true);
			    m.invoke(ret, value);
			    flag = true;
			    break;
			}
		    }
		} catch (Exception e) {

		}
		if (flag) {
		    continue;
		}
		f.setAccessible(true);
		// modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

		try {
		    f.set(ret, value);
		} catch (Exception e1) {
		    e1.printStackTrace();
		}
	    }
	} catch (Exception e1) {
	    e1.printStackTrace();
	}
	return ret;
    }

    public static Properties getProperties(String file) {
	Properties pro = new Properties();
	String path = ClassLoader.getSystemResource(file).getPath();
	try (InputStream fs = new FileInputStream(path);) {
	    pro.load(fs);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return pro;
    }
}