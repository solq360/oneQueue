package com.eyu.onequeue.demo.rmi.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITestRMIService extends Remote {
    //必须加RemoteException否则运行出错： java.rmi.server.ExportException
    public String a(String content) throws RemoteException;
    
    public TestRMIObj b(TestRMIObj input) throws RemoteException;
}