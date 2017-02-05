package com.eyu.onequeue.protocol.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface QModel {
    short value();
    
    public static final short QPRODUCE =1;
    public static final short QRESULT =2;
}
