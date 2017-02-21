package com.eyu.onequeue.proxy.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QEnhanceException;
import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.proxy.model.ClassMetadata;
import com.eyu.onequeue.proxy.model.IEnhanceService;
import com.eyu.onequeue.proxy.model.IProxy;
import com.eyu.onequeue.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;

/**
 * Javassist 代理实现
 * 
 * @author solq
 * @version 2014-3-6 上午11:20:38
 */
@SuppressWarnings({ "unchecked" })
public class JavassistProxy implements IProxy {
    private static final Logger logger = LoggerFactory.getLogger(JavassistProxy.class);

    /** 增强类后缀 */
    public static final String CLASS_SUFFIX = "$PROXY";
    /** 增强类域名:实体域(主要是数据隔离) */
    public static final String FIELD_ENTITY = "entity";

    /** 增强类的构造器映射 */
    private static final ConcurrentHashMap<Class<?>, ClassMetadata> proxyClassMetadatas = new ConcurrentHashMap<>();

    /** 增强类的服务映射 */
    private static final Map<Class<?>, IEnhanceService> enhanceServices = new HashMap<>();

    private static final IProxy DEFAULT_OBJECT = new JavassistProxy();

    public static IProxy getDefault() {
	return DEFAULT_OBJECT;
    }

    public void register(Class<?> clz, IEnhanceService service) {
	IEnhanceService old = enhanceServices.get(clz);
	if (old != null) {
	    return;
	}
	synchronized (clz) {
	    old = enhanceServices.get(clz);
	    if (old != null) {
		return;
	    }
	    enhanceServices.put(clz, service);
	}

    }

    @Override
    public <T> T transform(T entity) {
	Class<T> clz = (Class<T>) entity.getClass();
	try {
	    T result = null;
	    // 需要转换
	    if (!clz.getName().endsWith(CLASS_SUFFIX)) {
		Constructor<T> constructor = (Constructor<T>) getConstructor(clz);
		result = (T) constructor.newInstance(entity);
		enhanceServices.get(clz).loadInit(result);
	    } else {
		result = entity;
	    }
	    return result;
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.arrayFormat("实体类[{}]增强失败:{}", new Object[] { clz.getSimpleName(), e.getMessage(), e });
	    logger.error(message.getMessage());
	    throw new QEnhanceException(QCode.ENHANCE_ERROR, message.getMessage(), e);
	}
    }

    @Override
    public <T> T transform(Class<T> clz) {
	try {
	    Constructor<T> constructor = (Constructor<T>) getConstructor(clz);
	    T result = null;
	    if (clz.isInterface()) {
		result = (T) constructor.newInstance();
	    } else {
		result = (T) constructor.newInstance(clz.newInstance());
	    }
	    enhanceServices.get(clz).loadInit(result);
	    return result;
	} catch (Exception e) {
	    FormattingTuple message = MessageFormatter.arrayFormat("实体类[{}]增强失败:{}", new Object[] { clz.getSimpleName(), e.getMessage(), e });
	    logger.error(message.getMessage());
	    throw new QEnhanceException(QCode.ENHANCE_ERROR, message.getMessage(), e);
	}
    }

    /**
     * 获取增强类构造器
     * 
     * @param clz
     *            实体类
     * @return
     */
    private <T> Constructor<T> getConstructor(Class<T> clz) throws Exception {
	ClassMetadata classMetadata = proxyClassMetadatas.get(clz);
	if (classMetadata != null) {
	    return (Constructor<T>) classMetadata.getConstructor();
	}
	synchronized (clz) {
	    classMetadata = proxyClassMetadatas.get(clz);
	    if (classMetadata != null) {
		return (Constructor<T>) classMetadata.getConstructor();
	    }
	    return (Constructor<T>) createEnhancedClass(clz);
	}
    }

    /**
     * 创建增强类构造器
     * 
     * @param clz
     *            实体类
     * @return 增强类对象
     */
    private Constructor<?> createEnhancedClass(final Class<?> clz) throws Exception {
	final IEnhanceService enhanceService = enhanceServices.get(clz);
	enhanceService.initMetadata(clz);
	final ClassMetadata classMetadata = enhanceService.getClassMetadata(clz);
	classMetadata.init(clz, null, null, enhanceService);

	ReflectUtil.foreachMethods(clz, (method) -> {
	    if (Modifier.isFinal(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
		return;
	    }
	    if (JavassistHepler.OBJECT_METHODS.contains(method)) {
		return;
	    }
	    classMetadata.record(method);
	});

	// 创建增强类定义
	final CtClass enhancedClz = buildCtClass(clz);
	// 扩展处理
	enhanceService.addCtFields(clz, enhancedClz); // 添加扩展属性
	enhanceService.addCtConstructors(clz, enhancedClz); // 添加扩展构造
	enhanceService.addCtMethods(clz, enhancedClz); // 添加扩展方法
	enhanceService.buildEnhanceMethods(clz, enhancedClz); // 处理增强方法

	if (!clz.isInterface()) {
	    // 创建实体域
	    buildFields(clz, enhancedClz);
	    // 创建构造方法
	    buildConstructor(clz, enhancedClz);
	} else {
	    enhancedClz.addConstructor(CtNewConstructor.defaultConstructor(enhancedClz));
	}

	Class<?> current = enhancedClz.toClass();
	Constructor<?> constructor = null;
	if (clz.isInterface()) {
	    constructor = current.getConstructor();
	} else {
	    constructor = current.getConstructor(clz);
	}
	// 记录信息
	classMetadata.init(clz, current, constructor, enhanceService);
	proxyClassMetadatas.put(clz, classMetadata);
	return constructor;
    }

    /**
     * 创建代理对象属性
     */
    private void buildFields(Class<?> entityClz, CtClass enhancedClz) throws Exception {
	// 创建实体域
	CtField entityField = new CtField(JavassistHepler.classPool.get(entityClz.getName()), FIELD_ENTITY, enhancedClz);
	entityField.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
	// 添加JsonIgnore的注释到方法
	entityField.getFieldInfo2().addAttribute(JavassistHepler.addAnno(JsonIgnore.class, enhancedClz));
	enhancedClz.addField(entityField);
    }

    /** 创建增强类对象的方法 */
    // private void buildEnhanceMethod(Class<?> clz, CtClass enhancedClz, Method
    // method, String body) throws Exception {
    // // 创建方法定义
    // Class<?> returnType = method.getReturnType();
    // CtMethod ctMethod = new
    // CtMethod(JavassistHepler.classPool.get(returnType.getName()),
    // method.getName(),
    // JavassistHepler.toCtClassArray(method.getParameterTypes()), enhancedClz);
    // ctMethod.setModifiers(method.getModifiers());
    // if (method.getExceptionTypes().length != 0) {
    // ctMethod.setExceptionTypes(JavassistHepler.toCtClassArray(method.getExceptionTypes()));
    // }
    // StringBuilder bodyBuilder = new StringBuilder();
    // bodyBuilder.append("{");
    // bodyBuilder.append(body);
    // bodyBuilder.append("}");
    // ctMethod.setBody(bodyBuilder.toString());
    // enhancedClz.addMethod(ctMethod);
    // }

    /**
     * 创建增强类对象
     * 
     * <pre>
     * public class [entityClz.name]$PROXY extends [entityClz.name] {
     * }
     * </pre>
     */
    private CtClass buildCtClass(Class<?> entityClz) throws Exception {
	CtClass result = JavassistHepler.classPool.makeClass(entityClz.getCanonicalName() + CLASS_SUFFIX);
	if (!entityClz.isInterface()) {
	    result.setSuperclass(JavassistHepler.getCtClass(entityClz));
	} else {
	    result.setInterfaces(JavassistHepler.toCtClassArray(entityClz));
	}
	return result;
    }

    /**
     * 创建增强类的构造方法
     * 
     * <pre>
     * public [enhancedClz.name]([entityClz.name] entity ) {
     *     this.entity = entity;
     * }
     * </pre>
     */
    private void buildConstructor(Class<?> entityClz, CtClass enhancedClz) throws Exception {
	CtConstructor constructor = new CtConstructor(JavassistHepler.toCtClassArray(entityClz), enhancedClz);
	StringBuilder bodyBuilder = new StringBuilder();
	bodyBuilder.append("{");
	bodyBuilder.append("this." + FIELD_ENTITY + " = $1;");
	bodyBuilder.append("}");
	constructor.setBody(bodyBuilder.toString());
	constructor.setModifiers(Modifier.PUBLIC);
	enhancedClz.addConstructor(constructor);
    }

}
