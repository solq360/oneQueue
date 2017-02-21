package com.eyu.onequeue.bytebuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import sun.nio.ch.DirectBuffer;

public class DirectByteBufferTest {
    public static void main(String[] args) throws InterruptedException {
	testa();
	testb();
    }

    private static void testa() throws InterruptedException {
	printlnDirectInfo("test a");
	// 分配512MB直接缓存
	ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024 * 512);
 	printlnDirectInfo("init");
	// 清除直接缓存
	((DirectBuffer) bb).cleaner().clean();
	TimeUnit.SECONDS.sleep(2);
	printlnDirectInfo("clear");
	System.out.println("end");
    }
    
    private static void testb() throws InterruptedException {
	printlnDirectInfo("test b");
	// 分配512MB直接缓存
	ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024 * 512);
 	printlnDirectInfo("init");
	// 删除引用
 	bb=null;
 	System.gc();
	TimeUnit.SECONDS.sleep(2);
	printlnDirectInfo("clear");
	System.out.println("end");
    }

    private static void printlnDirectInfo(String tag) {
	try {
	    Class<?> c = Class.forName("java.nio.Bits");
	    Field field1 = c.getDeclaredField("maxMemory");
	    field1.setAccessible(true);
	    Field field2 = c.getDeclaredField("reservedMemory");
	    field2.setAccessible(true);
	    synchronized (c) {
 		Object max = (Object) field1.get(null);
		Object reserve = (Object) field2.get(null);
		System.out.println(tag + " ##### " +max + " " + reserve);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}