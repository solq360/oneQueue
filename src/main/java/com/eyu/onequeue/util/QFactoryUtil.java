package com.eyu.onequeue.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/****
 * Factory 保存全局对象
 * @author solq
 * */
public abstract class QFactoryUtil {
    private static Map<String, Object> values = Collections.synchronizedMap(new HashMap<>(10));

    public static final String CLIENT_DEFAULT_BOOTSTRAP = "CLIENT_DEFAULT_BOOTSTRAP";
    public static final String STORE_SERVER_SERVICE = "STORE_SERVER_SERVICE";
    public static final String STORE_CLIENT_SERVICE = "STORE_CLIENT_SERVICE";

    public static void putValue(String key, Object value) {
	values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(String key) {
	return (T) values.get(key);
    }

    public static void clear() {
	values.clear();
    }
}
