package com.eyu.onequeue.demo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.callback.model.QResult;
import com.eyu.onequeue.exception.QCode;
import com.eyu.onequeue.exception.QRpcException;
import com.eyu.onequeue.proxy.JavassistHepler;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.rpc.model.IRpcContext;
import com.eyu.onequeue.rpc.model.IRpcReceive;
import com.eyu.onequeue.rpc.model.IRpcSend;
import com.eyu.onequeue.rpc.model.RpcContext;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.util.ReflectUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

/****
 * 
 * @author solq
 */
@SuppressWarnings("unchecked")
public abstract class QRpcFactory3 {
    private static Method SEND_METHOD = ReflectUtil.getMethod(IRpcSend.class, "send");
    private static Method RECEIVE_METHOD = ReflectUtil.getMethod(IRpcReceive.class, "receive");

    private static Map<Short, Class<?>> SEND_CLASS = new HashMap<>();
    private static Map<Short, IRpcReceive> RECEIVE = new HashMap<>();
    private static Map<Short, Map<Byte, Method>> RECEIVE_METHOD_INFO = new HashMap<>();

    public static <T> T loadSystemService(Class<T> target) {
	T ret = null;
  	if (QMConfig.getInstance().isRemoteService(target)) {
	    // read config
	    String address = QMConfig.getInstance().REMOTE_SERVICE.get(target.getName());
	    ret = loadSendProxy(target, address);
	} else {
	    ret = loadLocalProxy(target);
	}
	return ret;
    }
    

    public static <T> T loadSendProxy(Class<T> target, QNode... nodes) {
	T ret = loadSendPorxy0(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(nodes));
	return ret;
    }

    public static <T> T loadSendProxy(Class<T> target, Long... ids) {
	T ret = loadSendPorxy0(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(ids));
	return ret;
    }

    public static <T> T loadSendProxy(Class<T> target, String... addresses) {
	T ret = loadSendPorxy0(target);
	IRpcContext ctx = (IRpcContext) ret;
	ctx.setContext(RpcContext.of(addresses));
	return ret;
    }

    private static <T> T loadSendPorxy0(Class<T> target) {
	QModel modelAnno = ReflectUtil.getAnno(target, QModel.class);
	Class<?> proxyClass = SEND_CLASS.get(modelAnno.value());
	T ret = null;
	try {
	    ret = (T) proxyClass.newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
	return ret;
    }

    public static <T> T loadLocalProxy(Class<T> target) {
	QModel modelAnno = ReflectUtil.getAnno(target, QModel.class);
	Object ret = RECEIVE.get(modelAnno.value());
	return (T) ret;
    }

    public static IRpcReceive loadReceiveProxy(short model) {
	IRpcReceive ret = RECEIVE.get(model);
	return ret;
    }

    // register
    public static <T> T registerReceiveProxy(Object obj) {
	Class<?> target = obj.getClass();
	if (target.isInterface()) {
	    throw new RuntimeException("class is Interface : " + target);
	}
	QModel modelAnno = ReflectUtil.getAnno(target, QModel.class);
	String proxyClassName = target.getCanonicalName() + "$$receive$$";
	ClassPool classPool = JavassistHepler.classPool;
	CtClass ctClass = classPool.makeClass(proxyClassName);

	try {
	    // 设置接口,继承 target
	    CtClass[] interfaces = new CtClass[1];
	    interfaces[0] = classPool.get(IRpcReceive.class.getName());
	    ctClass.setInterfaces(interfaces);
	    ctClass.setSuperclass(JavassistHepler.getCtClass(target));
	    {
		// 添加this字段
		final String ctxName = target.getName();
		CtField ctField = new CtField(classPool.get(ctxName), "_this", ctClass);
		ctField.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
		// 添加json 忽略anno
		ctField.getFieldInfo2().addAttribute(JavassistHepler.addAnno(JsonIgnore.class, ctClass));
		ctClass.addField(ctField);
	    }

	    Map<Byte, Method> methods = new HashMap<>();
	    RECEIVE_METHOD_INFO.put(modelAnno.value(), methods);

	    // 生成代理方法
	    ReflectUtil.foreachMethods(target, (method) -> {
		QCommond commond = method.getAnnotation(QCommond.class);
		if (commond == null) {
		    return;
		}
		methods.put(commond.value(), method);
		String resultType = "";
		if (void.class != method.getReturnType()) {
		    resultType = " return ($r) ";
		}
		final String body = "{ " + resultType + "_this." + method.getName() + "($$); }";
		JavassistHepler.addMethod(ctClass, method, body);
	    });

	    // 生成receive method
	    {
		final String body = "{return ($r) " + QRpcFactory3.class.getName() + ".proxyReceive(_this,$2, (short)" + modelAnno.value() + "  ,(byte) $1);}";
		JavassistHepler.addMethod(ctClass, RECEIVE_METHOD, body);
	    }

	    // 添加构造方法 new XXProxy(XX)
	    CtConstructor ctConstructor = new CtConstructor(JavassistHepler.toCtClassArray(target), ctClass);
	    ctConstructor.setBody("{ this._this = $1; }");
	    ctConstructor.setModifiers(Modifier.PUBLIC);
	    ctClass.addConstructor(ctConstructor);
	    Class<?> newClass = ctClass.toClass();
	    Constructor<T> constructor = (Constructor<T>) newClass.getConstructor(target);
	    constructor.setAccessible(true);
	    ctClass.detach();
	    Object ret = constructor.newInstance(obj);
	    RECEIVE.put(modelAnno.value(), (IRpcReceive) ret);
	    return (T) ret;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public static void registerSendProxy(Class<?> target) {
	if (!target.isInterface()) {
	    throw new RuntimeException("class is not  Interface : " + target);
	}
	QModel modelAnno = ReflectUtil.getAnno(target, QModel.class);
	String proxyClassName = target.getCanonicalName() + "$$send$$";
	ClassPool classPool = JavassistHepler.classPool;
	CtClass ctClass = classPool.makeClass(proxyClassName);

	try {
	    // 设置接口
	    CtClass[] interfaces = new CtClass[3];
	    interfaces[0] = classPool.get(target.getName());
	    interfaces[1] = classPool.get(IRpcSend.class.getName());
	    interfaces[2] = classPool.get(IRpcContext.class.getName());
	    ctClass.setInterfaces(interfaces);

	    {
		// 添加ctx字段
		final String ctxName = RpcContext.class.getName();
		CtField ctField = new CtField(classPool.get(ctxName), "ctx", ctClass);
		ctField.setModifiers(Modifier.PRIVATE);
		ctClass.addField(ctField);
		// 添加ctx get set 方法
		CtMethod ctMethod = CtMethod.make("public " + ctxName + " getContext(){return ctx;}", ctClass);
		ctMethod.setModifiers(Modifier.PUBLIC);
		ctClass.addMethod(ctMethod);

		ctMethod = CtMethod.make("public void setContext(" + ctxName + " value){ ctx =value;}", ctClass);
		ctMethod.setModifiers(Modifier.PUBLIC);
		ctClass.addMethod(ctMethod);
	    }

	    // 生成send method 调用静态方法减少书写复杂
	    {
		final String body = "{ return ($r) " + QRpcFactory3.class.getName() + ".proxySend(this,$2, (short)" + modelAnno.value() + "  ,(byte) $1);}";
		JavassistHepler.addMethod(ctClass, SEND_METHOD, body);
	    }

	    // 生成代理方法
	    for (Method method : target.getDeclaredMethods()) {
		QCommond commond = method.getAnnotation(QCommond.class);
		if (commond == null) {
		    continue;
		}

		String resultType = "";
		if (void.class != method.getReturnType()) {
		    resultType = " return ($r) ";
		}
		final String body = "{ " + resultType + " this.send((byte)" + commond.value() + ",$args); }";
		JavassistHepler.addMethod(ctClass, method, body);
	    }

	    // 保存记录
	    Class<?> newClass = ctClass.toClass();
	    ctClass.detach();
	    SEND_CLASS.put(modelAnno.value(), newClass);
 	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public static QResult proxySend(IRpcContext target, Object[] args, short model, byte commondIndex) {
	System.out.println("model : " + model + " commondIndex : " + commondIndex);
	System.out.println("args : " + SerialUtil.writeValueAsString(args));
	return null;
    }

    // 因为 javassist $$ 表达式访问的 参数类型 为 object 获取不到目标类型，所以只能用 invoke 处理
    public static Object proxyReceive(Object target, Object[] args, short model, byte commondIndex) {
	Map<Byte, Method> methods = RECEIVE_METHOD_INFO.get(model);
	try {
	    return methods.get(commondIndex).invoke(target, args);
	} catch (Exception e) {
	    throw new QRpcException(QCode.ENHANCE_ERROR_RPC_NOFIND_MODEL, "proxyReceive ", e);
	}
    }
}
