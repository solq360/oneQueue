package com.eyu.onequeue.other;

import java.util.HashSet;
import java.util.Set;

import com.eyu.onequeue.util.CodeTotal;

public class TestProjectWorkload {

    public static void main(String[] args) {
	Set<String> include = new HashSet<>();
	include.add(".java");
	CodeTotal.calculate("E:/java/oneQueue",include);
     }
}
