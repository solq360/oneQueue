package com.eyu.onequeue.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;

//import net.sf.cglib.proxy.Enhancer;
//import net.sf.cglib.proxy.MethodInterceptor;
//import net.sf.cglib.proxy.MethodProxy;

public class TestProxy {
    public interface UserService {
	public String getName(int id);

	public Integer getAge(int id);
    }

    public static class UserServiceImpl implements UserService {

	@Override
	public String getName(int id) {
	    return "name : " + id;
	}

	@Override
	public Integer getAge(int id) {
	    return id;
	}
    };

    public static void main(String[] args) throws Exception {
	testNative();
	testJdk();
	testCglib();
	testJavassist();
	System.in.read();
    }

    public static void testJavassist() throws Exception {
	String proxyClassName = UserService.class.getCanonicalName() + "$$$$";

	ClassPool classPool = ClassPool.getDefault();
	CtClass ctClass = classPool.makeClass(proxyClassName);

	// 设置接口
	CtClass[] interfaces = new CtClass[1];
	interfaces[0] = classPool.get(UserService.class.getName());
	ctClass.setInterfaces(interfaces);

	// 添加 方法
	for (Method method : UserService.class.getDeclaredMethods()) {
	    String body = null;
	    switch (method.getName()) {
	    case "getName":
		body = "{return $1 + \"\";}";
		break;
	    case "getAge":
		body = "{return ($w)$1;}";
		break;
	    default:
		continue;
	    }

	    Class<?> returnType = method.getReturnType();
	    // 转换参数CtClass
	    CtClass[] parameterCtClass = new CtClass[method.getParameterTypes().length];
	    for (int i = 0; i < method.getParameterTypes().length; i++) {
		parameterCtClass[i] = classPool.get(method.getParameterTypes()[i].getName());
	    }
	    CtMethod ctMethod = new CtMethod(classPool.get(returnType.getName()), method.getName(), parameterCtClass, ctClass);
	    ctMethod.setModifiers(method.getModifiers());
	    ctMethod.setBody(body);
	    ctClass.addMethod(ctMethod);

	}
	// 添加构造
	ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
	
	Class<?> clz = ctClass.toClass();
	UserService proxy = (UserService) clz.newInstance();
	run("javassist", proxy);
    }

    public static void testJdk() {
	UserService impTarget = new UserServiceImpl();
	// 代理处理逻辑
	InvocationHandler handler = new InvocationHandler() {

	    @Override
	    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
		return method.invoke(impTarget, args);
	    }
	};
	// Proxy.newProxyInstance(ClassLoader loader/**ClassLoader 没有特别处理 拿默认即可
	// **/, Class<?>[] interfaces/**代理接口类**/, InvocationHandler
	// h/**代理处理逻辑**/)
	UserService proxy = (UserService) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { UserService.class }, handler);

	run("jdk", proxy);
    }

    public static void testNative() {
	UserService impTarget = new UserServiceImpl();
	run("native", impTarget);
    }

    public static void testCglib() {
	// Enhancer enhancer = new Enhancer();
	// enhancer.setSuperclass(UserServiceImpl.class);
	// enhancer.setCallback(new MethodInterceptor() {
	//
	// @Override
	// public Object intercept(Object obj, Method method, Object[] args,
	// MethodProxy proxy) throws Throwable {
	// return proxy.invokeSuper(obj, args);
	//
	// }
	// });
	// UserService impTarget = (UserService) enhancer.create();
	// run("cglib",impTarget);
    }

    private static void run(String tag, UserService impTarget) {
	int c = 15;
	System.out.println();
	while (c-- > 0) {
	    long start = System.currentTimeMillis();
	    for (int i = 0; i < 10000000; i++) {
		impTarget.getName(11);
	    }
	    long end = System.currentTimeMillis();
	    System.out.print(tag + ": " + (end - start) + " ");
	}
    }
}
