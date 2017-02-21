package com.eyu.onequeue.rpc.codec;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

/***
 * 
 * @author solq
 */
public abstract class TypeCode {
    public final static byte NUM = 1;
    public final static byte STRING = 2;
    public final static byte ENUM = 3;
    public final static byte BOOLEAN = 4;
    public final static byte MAP = 5;
    public final static byte COLLECTION = 6;
    public final static byte BYTES = 7;
    public final static byte CLASS = 8;
    public final static byte DATE = 9;
    public static final byte OBJECT = 10;
    public final static byte SHORT = 11;
    public static final byte BYTE = 12;

    public final static Map<Byte, IQParse> ACTIONS = new HashMap<>();

    static {
	register(new QBooleanParse());
	register(new QDateParse());
	register(new QMapParse());
	register(new QCollectionParse());
	register(new QClassParse());
	register(new QNumParse());
	register(new QBytesParse());
	register(new QStringParse());
	register(new QObjectParse());
	register(new QShortParse());
	register(new QByteParse());
    }

    public static void register(IQParse parse) {
	if (ACTIONS.containsKey(parse.getType())) {
	    throw new RuntimeException("注册JAVA TYPE 解释有冲突  : " + parse.getType() + " : " + parse.getClass());
	}
	ACTIONS.put(parse.getType(), parse);
    }

    public static byte getCode(Type type) {
	if (TypeUtils.isAssignable(type, Date.class)) {
	    return TypeCode.DATE;
	} else if (TypeUtils.isAssignable(type, String.class)) {
	    return TypeCode.STRING;
	} else if (TypeUtils.isAssignable(type, Short.class)) {
	    return TypeCode.SHORT;
	} else if (TypeUtils.isAssignable(type, Byte.class)) {
	    return TypeCode.BYTE;
	} else if (TypeUtils.isAssignable(type, Number.class)) {
	    return TypeCode.NUM;
	} else if (type.equals(Boolean.TYPE)) {
	    return TypeCode.BOOLEAN;
	} else if (TypeUtils.isAssignable(type, Map.class)) {
	    return TypeCode.MAP;
	} else if (TypeUtils.isAssignable(type, Collection.class)) {
	    return TypeCode.COLLECTION;
	} else if (TypeUtils.isArrayType(type)) {
	    return TypeCode.COLLECTION;
	} else if (TypeUtils.isAssignable(type, Class.class)) {
	    return TypeCode.CLASS;
	} else if (TypeUtils.isAssignable(type, byte[].class)) {
	    return TypeCode.BYTES;
	} else {
	    return TypeCode.OBJECT;
	}
    }
}