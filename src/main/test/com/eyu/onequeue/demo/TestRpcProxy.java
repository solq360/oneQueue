package com.eyu.onequeue.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eyu.onequeue.callback.model.QResult;
import com.eyu.onequeue.callback.model.QResultWrapper;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.rpc.model.IRpcReceive;
import com.eyu.onequeue.util.ReflectUtil;

public class TestRpcProxy {
    @QModel(1)
    public interface TestObject {
	@QCommond(1)
	public void a(int a, String b);

	@QCommond(2)
	public void setAge(int value);

	@QCommond(3)
	public QResult<Integer> getAge();

	@QCommond(4)
	public void setObj(TestObject1 obj);
	
	@QCommond(5)
	public void test(int a,Integer b,Double c ,List<Integer> d,Integer[] e );
    }

    public class TestObject1 {

	public int a = 67;

    }

    public static class TestObjectImpl implements TestObject {
	private int age;

	@Override
	public void a(int a, String b) {
	    System.out.println("a : " + a + " " + b);
	}

	@Override
	public void setAge(int value) {
	    age = value;
	}

	@Override
	public QResult<Integer> getAge() {
	    return QResultWrapper.of(age);
	}

	@Override
	public void setObj(TestObject1 obj) {
	    System.out.println(obj.a);
	}

	@Override
	public void test(int a, Integer b, Double c ,List<Integer> d,Integer[] e  ) {
 	    
	}
    }

    @Test
    public void testSend() {
	QRpcFactory3.registerSendProxy(TestObject.class);
	TestObject proxy = QRpcFactory3.loadSendProxy(TestObject.class, 1L);

	proxy.a(1, "b");
	QResult<Integer> ret = proxy.getAge();
    }

    @Test
    public void testReceive() {	
	TestObject proxy = QRpcFactory3.registerReceiveProxy(new TestObjectImpl());

	proxy.a(1, "b");
	proxy.setAge(30);
	QResult<Integer> ret = proxy.getAge();
	System.out.println(ret.getResult());

	Object[] args = new Object[1];
	args[0] =18;
		
	((IRpcReceive) proxy).receive((byte) 2, args);

	ret = proxy.getAge();
	System.out.println(ret.getResult());
 	args[0] = new TestObject1();
	((IRpcReceive) proxy).receive((byte) 4, args);
    }
    
    @Test
    public void testObjectArgs() {
	QRpcFactory3.registerReceiveProxy(new TestObjectImpl());
 	IRpcReceive obj = QRpcFactory3.loadReceiveProxy((short)1);
 	int a=30;
 	Integer b= 30;
 	double c=1d;
 	List<Integer> d = new ArrayList<>();
 	Integer[] e = new Integer[0];
	Object[] args = new Object[5];
	args[0] =a;
	args[1] =b;
	args[2] =c;
	args[3] =d;
	args[4] =e;

	obj.receive((byte)5, args);
    }

     static void testObjectArgs(Object[] args) {
	Method method =ReflectUtil.getMethod(TestObject.class, "a");
	method.setAccessible(true);
	try {
	    method.invoke(new TestObjectImpl(), args);
	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
	} catch (IllegalArgumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InvocationTargetException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
