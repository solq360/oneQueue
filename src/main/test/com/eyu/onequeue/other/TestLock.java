package com.eyu.onequeue.other;

public class TestLock {

    Object lock = new Object();

    private int value = 0 ;
    public static void main(String[] args) throws InterruptedException {
	TestLock testLock = new TestLock();

	int count = 500;
	Thread[] threads = new Thread[count];
	for (int i = 0; i < count; i++) {
	    int t = i;
	    threads[i] = new Thread(new Runnable() {
		public void run() {
		    if (t % 2 == 0) {
			testLock.lockFn1();
		    } else {
			testLock.lockFn2();
		    }
		}
	    });
	}
	for (int i = 0; i < count; i++) {
	    threads[i].start();
	}
	for (int i = 0; i < count; i++) {
	    threads[i].join();
	}
	System.out.println(testLock.value);
    }

    private void lockFn1() {
	synchronized (lock) 
	{
	    lockFn2();
	    lockFn3();
	}
    }

    private void lockFn2() {
	synchronized (lock) 
	{
	    value++;
	}
    }

    private void lockFn3() {
	synchronized (lock) 
	{
	    lockFn2();
	}
    }
}
