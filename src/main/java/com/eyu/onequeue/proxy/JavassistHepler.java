package com.eyu.onequeue.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * Javassist 帮助工具
 * 
 * @author solq
 * @version 2014-3-6 上午11:20:38
 */
public abstract class JavassistHepler {

    /** 增强的类池 */
    public static final ClassPool classPool = ClassPool.getDefault();

    static {
	classPool.insertClassPath(new ClassClassPath(JavassistHepler.class));
    }
    public static Set<Method> OBJECT_METHODS = new HashSet<>();

    static {
	for (Method m : Object.class.getDeclaredMethods()) {
	    OBJECT_METHODS.add(m);
	}
    }
    
    public static CtClass getCtClass(String clzName) {
	try {
	    return classPool.get(clzName);
	} catch (NotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

    public static CtClass getCtClass(Class<?> clz) {
	return getCtClass(clz.getName());
    }

    /**
     * 将{@link Class}转换为{@link CtClass}
     */
    public static CtClass[] toCtClassArray(Class<?>... classes) {
	if (classes == null || classes.length == 0) {
	    return new CtClass[0];
	}
	CtClass[] result = new CtClass[classes.length];
	for (int i = 0; i < classes.length; i++) {
	    result[i] = getCtClass(classes[i]);
	}
	return result;
    }

    /** 获取方法返回类型字符 */
    public static String getMethodReturn(Method method) {
	Class<?> returnType = method.getReturnType();
	if (!returnType.isArray()) {
	    return returnType.getName();
	}
	Class<?> type = returnType.getComponentType();
	return type.getName() + "[]";
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getParamAnno(Method method, Class<T> anno, int index) {
	Annotation[] annos = method.getParameterAnnotations()[index];
	for (int i = 0; i < annos.length; i++) {
	    if (annos[i].annotationType().isAssignableFrom(anno)) {
		return (T) annos[i];
	    }
	}
	return null;
    }

    /**
     * 添加anno
     */
    public static AnnotationsAttribute addAnno(Class<? extends java.lang.annotation.Annotation> anno, CtClass enhancedClz) {
	ConstPool cp = enhancedClz.getClassFile2().getConstPool();
	AnnotationsAttribute annoAttr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
	javassist.bytecode.annotation.Annotation annot = new javassist.bytecode.annotation.Annotation(anno.getName(), cp);
	annoAttr.addAnnotation(annot);
	return annoAttr;
    }

    /**
     * 添加方法
     */
    public static void addMethod(CtClass ctClass, Method method, String body) {
 	CtMethod ctMethod = new CtMethod(getCtClass(method.getReturnType().getName()), method.getName(), toCtClassArray(method.getParameterTypes()), ctClass);
	ctMethod.setModifiers(method.getModifiers());
	try {
	    if (method.getExceptionTypes().length != 0) {
		ctMethod.setExceptionTypes(toCtClassArray(method.getExceptionTypes()));
	    }
	    ctMethod.setBody(body);
	    ctClass.addMethod(ctMethod);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * 静态方法有问题
     */
    protected static String[] getMethodParamNames(CtMethod cm) {
	MethodInfo methodInfo = cm.getMethodInfo2();
	CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
	LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

	String[] paramNames = null;
	try {
	    paramNames = new String[cm.getParameterTypes().length];
	} catch (NotFoundException e) {
	}
	int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
	for (int i = 0; i < paramNames.length; i++) {
	    paramNames[i] = attr.variableName(pos + i);
	}
	return paramNames;
    }

    /**
     * 获取方法参数名称 类不能是接口
     */
    public static String[] getMethodParamNames(Method method) {
	CtClass clz = getCtClass(method.getDeclaringClass());
	CtClass[] params = new CtClass[method.getParameterTypes().length];
	for (int i = 0; i < method.getParameterTypes().length; i++) {
	    params[i] = getCtClass(method.getParameterTypes()[i].getName());
	}
	try {
	    CtMethod cm = clz.getDeclaredMethod(method.getName(), params);
	    return getMethodParamNames(cm);
	} catch (NotFoundException e) {
	    throw new RuntimeException(e);
	}
    }
}
