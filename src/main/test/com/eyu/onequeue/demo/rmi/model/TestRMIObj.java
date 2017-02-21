package com.eyu.onequeue.demo.rmi.model;

import java.io.Serializable;

//必须实现Serializable该对象要支持 Serializable
public class TestRMIObj implements Serializable {

    private static final long serialVersionUID = 2367853017282270281L;
    private int a;
    private Integer[] b;

    public int getA() {
	return a;
    }

    public Integer[] getB() {
	return b;
    }

    public void setA(int a) {
	this.a = a;
    }

    public static TestRMIObj of(int a, Integer... b) {
	TestRMIObj ret = new TestRMIObj();
	ret.a = a;
	ret.b = b;
	return ret;
    }
}
