package com.eyu.onequeue.proxy.model;

import java.lang.reflect.Method;

import javassist.CtClass;

/**
 * 增强服务接口
 * 
 * @author solq
 */
public interface IEnhanceService {

    /** 对象初始化时 */
    public void loadInit(Object obj);

    /*** 添加CtConstructor */
    public void addCtConstructors(Class<?> clz, CtClass enhancedClz) throws Exception;

    /*** 添加CtField */
    public void addCtFields(Class<?> clz, CtClass enhancedClz) throws Exception;

    /*** 添加CtMethods */
    public void addCtMethods(Class<?> clz, CtClass enhancedClz) throws Exception;

    /*** 构造增强方法 */
    public void buildEnhanceMethods(Class<?> clz, CtClass enhancedClz ) throws Exception;

    /** 过滤method */
    public boolean matches(Method method);

    public void initMetadata();

    /** 获取类元信息 */
    public ClassMetadata getClassMetadata();
}