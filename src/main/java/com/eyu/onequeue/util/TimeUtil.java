package com.eyu.onequeue.util;

public abstract class TimeUtil {

    private static long record = System.currentTimeMillis();

    public static void record() {
	record = System.currentTimeMillis();
    }

    public static void println(String tag) {
	System.out.println(tag + (System.currentTimeMillis() - record));
    }
}
