package com.eyu.onequeue.demo.jmx.model;

public class TestMBeanServiceImpl implements ITestMBean {
    private static final long serialVersionUID = -6460154344452562895L;
    
    @Override
    public String a(String content) {
	System.out.println("call : a" + content);
	return "server >> " + content;
    }

    @Override
    public int b(int a) {
	stackTrace();
 	return a+2;
    }

    private void stackTrace() {
	StackTraceElement[] se=Thread.currentThread().getStackTrace();
	for(StackTraceElement s : se){
	    System.out.println(s.getClassName() + ":" + s.getMethodName());
  	    System.out.println();
	}	
    }
}