package com.eyu.onequeue.demo.rmi.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// UnicastRemoteObject用于导出的远程对象和获得与该远程对象通信的存根。否则运行出错：java.rmi.MarshalException
public class TestRMIServiceImpl extends UnicastRemoteObject implements ITestRMIService {
    private static final long serialVersionUID = -6460154344452562895L;

    public TestRMIServiceImpl() throws RemoteException {
	super();
    }

    @Override
    public String a(String content) {
	System.out.println("call : a"+ content );
	return "server >> " + content;
    }

    @Override
    public TestRMIObj b(TestRMIObj input) throws RemoteException {
	System.out.println("call b : "+ input.getB().length );
	input.setA(12);
	return input;
    }
}