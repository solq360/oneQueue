package com.eyu.onequeue.demo;

public class TestException {
    public static void main(String[] args) {
	new TestException().demo();
    }
//http://www.cnblogs.com/skywang12345/p/3344137.html
    //http://blog.csdn.net/doc_sgl/article/details/50367083
    
    //https://www.oschina.net/code/snippet_617922_52528
    
    //eclipse 模板 http://blog.csdn.net/zhan1350441670/article/details/47971695
    public void a() {
	try {
	    b();
	} catch (Exception e) {
	    System.out.println(e.getStackTrace().length);
	    throw new RuntimeException("call a", e);
	}

    }

    public void b() {
	try {
	    c();
	} catch (Exception e) {
	    throw new RuntimeException("call b", e);
	}
    }

    public void c() {
	throw new RuntimeException("call c");
    }

    public void demo() {
	a();
    }
}
