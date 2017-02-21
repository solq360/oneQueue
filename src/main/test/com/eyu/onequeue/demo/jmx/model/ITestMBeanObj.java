package com.eyu.onequeue.demo.jmx.model;

import java.io.Serializable;

import javax.management.MXBean;

@MXBean
public interface ITestMBeanObj extends Serializable {

    public int getA();

    public Integer[] getB();

    public void setA(int a);

}
