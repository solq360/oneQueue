package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;

/***
 * 
 * @author solq
 */
public interface IQParse {
    byte[] encode(Object obj);

    Object decode(Type type, byte[] bytes);

    byte getType();
}