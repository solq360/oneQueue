package com.eyu.onequeue.demo;

import java.lang.reflect.Field;

public class TestAnnotation {
    @TestAnno(value = 1, name = "a")
    private int a;

    public static void main(String[] args) {
	System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
 	for (Field field : TestAnnotation.class.getDeclaredFields()) {
	    TestAnno anno =   field.getAnnotation(TestAnno.class);
	    System.out.println(anno.value());
	}
    }
}
