package com.eyu.onequeue.protocol.model;

import java.util.Map;

import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;

/***
 * 队列消息
 * 
 * @author solq
 */
@JsonInclude(Include.NON_EMPTY)
public class QMessage<K, V> {
    /** 主健 **/
    private K k;
    /** 生成时间 **/
    private long t;
    /** 内容 **/
    private V b;
 

    @SuppressWarnings("rawtypes")
    public <T> T formatMessage(TypeReference<T> tr) {
	return SerialUtil.map2Object((Map) b, tr);
    }

    // getter

    public K getK() {
	return k;
    }

    public V getB() {
	return b;
    }

    public long getT() {
	return t;
    }
  
    public static <K, V> QMessage<K, V> of(K key, V value) {
	QMessage<K, V> ret = new QMessage<>();
	ret.k = key;
	ret.b = value;
	ret.t = System.currentTimeMillis();
	return ret;
    }

}
