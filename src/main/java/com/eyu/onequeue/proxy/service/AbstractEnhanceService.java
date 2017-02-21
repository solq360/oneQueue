package com.eyu.onequeue.proxy.service;

import java.lang.reflect.Method;

import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.proxy.model.IEnhanceService;

import javassist.CtClass;
import javassist.CtMethod;

/**
 * 增强服务模板
 * 
 * @author solq
 */
public abstract class AbstractEnhanceService implements IEnhanceService {

    @Override
    public void addCtConstructors(Class<?> clz, CtClass enhancedClz) throws Exception {
    }

    @Override
    public void addCtFields(Class<?> clz, CtClass enhancedClz) throws Exception {
    }

    @Override
    public void addCtMethods(Class<?> clz, CtClass enhancedClz) throws Exception {
    }

    @Override
    public void loadInit(Object obj) {

    }
 

    /**
     * 创建增强类对象的方法
     * 
     * @return
     */
    protected CtMethod buildEnhanceMethod(Class<?> clz, CtClass enhancedClz, Method method, String body) {
	// 创建方法定义
	try {
	    Class<?> returnType = method.getReturnType();
	    CtMethod ctMethod = new CtMethod(JavassistHepler.getCtClass(returnType.getName()), method.getName(), JavassistHepler.toCtClassArray(method.getParameterTypes()), enhancedClz);
	    ctMethod.setModifiers(method.getModifiers());
	    if (method.getExceptionTypes().length != 0) {
		ctMethod.setExceptionTypes(JavassistHepler.toCtClassArray(method.getExceptionTypes()));
	    }
	    StringBuilder bodyBuilder = new StringBuilder();
	    bodyBuilder.append("{");
	    bodyBuilder.append(body);
	    bodyBuilder.append("}");
	    ctMethod.setBody(bodyBuilder.toString());
	    enhancedClz.addMethod(ctMethod);
	    return ctMethod;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /** 创建增强类对象的普通代理方法 */
    protected CtMethod buildNormalMethod(Class<?> clz, CtClass enhancedClz, Method method) throws Exception {
	// 创建方法定义
	Class<?> returnType = method.getReturnType();
	CtMethod ctMethod = new CtMethod(JavassistHepler.classPool.get(returnType.getName()), method.getName(), JavassistHepler.toCtClassArray(method.getParameterTypes()), enhancedClz);
	ctMethod.setModifiers(method.getModifiers());
	if (method.getExceptionTypes().length != 0) {
	    ctMethod.setExceptionTypes(JavassistHepler.toCtClassArray(method.getExceptionTypes()));
	}

	StringBuilder bodyBuilder = new StringBuilder();
	bodyBuilder.append("{");
	if (returnType == void.class) {
	    bodyBuilder.append(JavassistProxy.FIELD_ENTITY + "." + method.getName() + "($$);");
	} else {
	    bodyBuilder.append(returnType.getName() + " result = " + JavassistProxy.FIELD_ENTITY + "." + method.getName() + "($$);");
	    bodyBuilder.append("return result;");
	}
	bodyBuilder.append("}");
	ctMethod.setBody(bodyBuilder.toString());
	enhancedClz.addMethod(ctMethod);
	return ctMethod;
    }
}