package com.eyu.onequeue.demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;

public class TestJavassist {
    public interface TestObject {
	public void a(int a, String b);

	public void setAge(int value);

	public int getAge();
    }

    public static void main(String[] args) throws Exception {
	String proxyClassName = TestObject.class.getCanonicalName() + "$$$$";
	ClassPool classPool = ClassPool.getDefault();
	CtClass ctClass = classPool.makeClass(proxyClassName);

	// 设置接口
	CtClass[] interfaces = new CtClass[1];
	interfaces[0] = classPool.get(TestObject.class.getName());
	ctClass.setInterfaces(interfaces);

	// 添加字段
	CtField ctField = new CtField(classPool.get(int.class.getName()), "age", ctClass);
	// 设置field属性
	ctField.setModifiers(Modifier.PRIVATE);
	ctClass.addField(ctField);

	// 添加 age getter'setter方法
	String setbody = "{this.age = $1;}";
	String getbody = "{return this.age;}";
	for (Method method : TestObject.class.getDeclaredMethods()) {
	    String body = null;
	    switch (method.getName()) {
	    case "setAge":
		body = setbody;
		break;
	    case "getAge":
		body = getbody;
		break;
	    case "a":
		body = "{System.out.println($1);System.out.println($2);}";
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
	TestObject obj = (TestObject) clz.newInstance();
	obj.setAge(30);
	System.out.println("age : " + obj.getAge());
	obj.a(111, "bbb");

	// ctClass.writeFile("f:/test.class");
    }
}
