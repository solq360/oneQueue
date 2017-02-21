package com.eyu.onequeue.demo.jmx.model;

import java.io.Serializable;

import javax.management.MXBean;

@MXBean
public interface ITestMBean extends Serializable {
    public String a(String content);
    public int b(int a);
}