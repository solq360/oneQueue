package com.eyu.onequeue.rpc;

import com.eyu.onequeue.proxy.service.JavassistProxy;
import com.eyu.onequeue.proxy.service.QRpcEnhanceService;

public class TestPackageScanner {

    public static void main(String[] args) {
	// PackageScanner packageScanner = new
	// PackageScanner("com.eyu.onequeue");
	// SerialUtil.println(packageScanner.getClazzCollection());
	// System.out.println(packageScanner.getClazzCollection().size());

	JavassistProxy.getDefault().register(TestEnhance.class, new QRpcEnhanceService(), true);
	TestEnhance proxyObject = JavassistProxy.getDefault().transform(TestEnhance.class);
	proxyObject.a(1, "asa");
    }

}
