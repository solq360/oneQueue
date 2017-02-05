package com.eyu.onqueue.other;

import java.util.LinkedList;
import java.util.List;

import com.eyu.onequeue.store.FileIndexer;
import com.eyu.onequeue.store.FileQMStore;

public class TestParallelStream{
//http://www.tuicool.com/articles/bIrYJvz
	public static void main(String[] args) {
		List<FileQMStore> list = new LinkedList<>();
		String topic="test1";
		list.add(FileQMStore.of(topic, FileIndexer.of(topic)));
		
		list.parallelStream().forEach(f ->{
			System.out.println("xxx");
		});
		
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		
	}

}
