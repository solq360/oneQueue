package com.eyu.onequeue.other;

public class TestException {

    public static void main(String[] args) {
	int v = 111;
	if (v > 0) {
	    throw build();

	}
	int b = 111;

    }

    public static RuntimeException build() {
	Exception ex = new RuntimeException("aaaa");
	return new RuntimeException("xxxxx", ex);
    }
}
