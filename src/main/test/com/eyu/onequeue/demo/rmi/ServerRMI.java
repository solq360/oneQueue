package com.eyu.onequeue.demo.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import com.eyu.onequeue.demo.rmi.model.ITestRMIService;
import com.eyu.onequeue.demo.rmi.model.TestRMIServiceImpl;

public class ServerRMI {
    public static void main(String[] args) {
	try {
	    // 实例化实现了IService接口的远程服务ServiceImpl对象
	    ITestRMIService service02 = new TestRMIServiceImpl();
	    // 本地主机上的远程对象注册表Registry的实例，并指定端口为8888
	    LocateRegistry.createRegistry(8888);
	    // 把远程对象注册到RMI注册服务器上，并命名为service02
	    // 绑定的URL标准格式为：rmi://host:port/name
	    Naming.bind("rmi://localhost:8888/service02", service02);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	System.out.println("服务器向命名表注册了1个远程服务对象！");
    }
}