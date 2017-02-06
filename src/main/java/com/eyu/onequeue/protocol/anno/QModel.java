package com.eyu.onequeue.protocol.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface QModel {
    byte value();
    
    public static final byte QPRODUCE =1;
    public static final byte QCONSUME =2;
    public static final byte QRPC =3;
    public static final byte QCODE =4;
}
