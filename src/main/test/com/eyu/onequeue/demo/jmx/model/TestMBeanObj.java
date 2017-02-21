package com.eyu.onequeue.demo.jmx.model;

public class TestMBeanObj implements ITestMBeanObj {

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

    public static TestMBeanObj of(int a, Integer... b) {
	TestMBeanObj ret = new TestMBeanObj();
	ret.a = a;
	ret.b = b;
	return ret;
    }
}
