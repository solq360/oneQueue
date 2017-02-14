package com.eyu.onequeue.protocol.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author solq
 **/
@Target(TYPE)
@Retention(RUNTIME)
public @interface QOpCode {
    short value();

    public static final short QPRODUCE = 1;
    public static final short QCONSUME = 2;
    public static final short QSUBSCIBE = 3;
    public static final short QCODE = 4;
    public static final short QRPC = 5;

}
