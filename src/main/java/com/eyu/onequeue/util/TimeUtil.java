package com.eyu.onequeue.util;

public abstract class TimeUtil {

    private static ThreadLocal<Long> local = new ThreadLocal<>();

    public static void record() {
	local.set(System.currentTimeMillis());
    }

    public static void record(String tag) {
	long record = System.currentTimeMillis();
	local.set(record);
	System.out.println(tag + record);
    }

    public static void println(String tag) {
	long record = local.get();
	System.out.println(tag + (System.currentTimeMillis() - record));
    }
}
