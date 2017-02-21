package com.eyu.onequeue.demo;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.demo.TestRpcProxy.TestObject;
import com.eyu.onequeue.rpc.ITestEnhance;
import com.eyu.onequeue.rpc.anno.QModel;
import com.eyu.onequeue.util.PackageScanner;
import com.eyu.onequeue.util.ReflectUtil;

public class TestScanClass {

    @Test
    public void testAll() {
	PackageScanner.scan((clz) -> {
	    // System.out.println(clz);
	});
    }

    @Test
    public void testSystemService() {
	Set<Class<?>> values = new HashSet<>();
	PackageScanner.scan((clz) -> {
	    QModel modelAnno = ReflectUtil.getAnno(clz, QModel.class);
	    if (modelAnno == null) {
		return;
	    }
	    values.add(clz);
	} , "com.eyu.onequeue");

	for (Class<?> clz : values) {
	    if (clz.isInterface()) {
		if (!QMConfig.getInstance().isRemoteService(clz)) {
		    continue;
		}
		QRpcFactory3.registerSendProxy(clz);
		System.out.println("registerSendProxy : " + clz);
	    } else {
		if (QMConfig.getInstance().isRemoteService(clz)) {
		    continue;
		}
		try {
		    QRpcFactory3.registerReceiveProxy(clz.newInstance());
		    System.out.println("registerReceiveProxy : " + clz);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	//test removet
	ITestEnhance obj = QRpcFactory3.loadSystemService(ITestEnhance.class);
	obj.a(1d);
	//test local
	TestObject obj1 = QRpcFactory3.loadSystemService(TestObject.class);
	obj1.a(1, "b");
    }

}
