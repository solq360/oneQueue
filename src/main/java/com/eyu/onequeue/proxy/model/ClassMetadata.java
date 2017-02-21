package com.eyu.onequeue.proxy.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.eyu.onequeue.protocol.model.IRecycle;

/***
 * @author solq
 */
public abstract class ClassMetadata implements IRecycle {
    /** 类 */
    protected Class<?> clz;
    /** 增强后的类 */
    protected Class<?> enhancedClz;
    protected Constructor<?> constructor;
    protected IEnhanceService enhanceService;

    public void init(Class<?> clz, Class<?> enhancedClz, Constructor<?> constructor, IEnhanceService enhanceService) {
	this.clz = clz;
	this.enhancedClz = enhancedClz;
	this.constructor = constructor;
	this.enhanceService = enhanceService;
    }

    public abstract void record(Method method);
    
    @Override
    public void recycle() {
	clz = null;
	enhancedClz = null;
	constructor = null;
	enhanceService = null;
    }
    
    // getter

    public Class<?> getEnhancedClz() {
	return enhancedClz;
    }

    public Constructor<?> getConstructor() {
	return constructor;
    }

    public Class<?> getClz() {
	return clz;
    }

    public IEnhanceService getEnhanceService() {
	return enhanceService;
    }
}
