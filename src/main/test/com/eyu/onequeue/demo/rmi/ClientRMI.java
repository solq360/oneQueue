package com.eyu.onequeue.demo.rmi;

import java.rmi.Naming;

import com.eyu.onequeue.demo.rmi.model.ITestRMIService;
import com.eyu.onequeue.demo.rmi.model.TestRMIObj;

public class ClientRMI {
    private ITestRMIService service;

    public void start() {
	String url = "rmi://localhost:8888/";
	try {
	    // 在RMI服务注册表中查找名称为service02的对象，并调用其上的方法
	    service = (ITestRMIService) Naming.lookup(url + "service02");
	    System.out.println(service.a("你好！"));
	    System.out.println(service.b(TestRMIObj.of(1, 2, 3, 4)).getA());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) throws Exception {
	ClientRMI rmi = new ClientRMI();
	rmi.start();
	while (true) {
	    Thread.sleep(500);
	    rmi.service.a("a");
	}
    }
}