package com.eyu.onequeue.rpc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author solq
 */
@Target({ ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface QRpcParam {
    boolean required() default true;
}
