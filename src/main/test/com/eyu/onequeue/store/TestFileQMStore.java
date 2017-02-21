package com.eyu.onequeue.store;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.eyu.onequeue.message.TestMessage;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.store.model.IQStore;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.service.FileIndexer;
import com.eyu.onequeue.store.service.FileQMStore;
import com.eyu.onequeue.util.FileUtil;
import com.eyu.onequeue.util.LockFreeList;
import com.eyu.onequeue.util.TimeUtil;

public class TestFileQMStore {
    String topic = "topic_test5";

    @Test
    public void testFreeList(){
	LockFreeList<Integer> list = new LockFreeList<>();
	long start = System.currentTimeMillis();
	for(int i=0;i<1000000;i++){
	    list.add(i);
	}
	long end = System.currentTimeMillis();

	System.out.println(end -start);
	System.out.println(list.size());
    }
    
    @Test
    public void test_frame() {
	QProduce qproduce = TestMessage.ofProduce(topic, 2000, 20);

	String topic = qproduce.getT();
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQStore store = FileQMStore.of(topic, fileIndexer);

	long start = System.currentTimeMillis();
	for (int i = 0; i < 10000; i++) {
	    store.save(qproduce.getB());
	}
	store.close();
	long end = System.currentTimeMillis();
	System.out.println("persist : " + (end - start));
    }

    @Test
    public void test_queryForeach() {
	FileIndexer fileIndexer = FileIndexer.of(topic);
	IQStore store = FileQMStore.of(topic, fileIndexer);
	TimeUtil.record();
	QConsume l = store.queryForRaw(QQuery.of(topic, 0));
	TimeUtil.println("raw query : ");

	AtomicInteger ai = new AtomicInteger();

	TimeUtil.record();
	l.foreachMessageData((list) -> {
	    ai.addAndGet(list.length);
	});
	System.out.println(ai.get());
	TimeUtil.println("raw decode : ");

	ai.set(0);
	TimeUtil.record();
	l = store.query(QQuery.of(topic, 0));
	TimeUtil.println("query : ");

	TimeUtil.record();
	l.foreachMessageData((list) -> {
	    ai.addAndGet(list.length);
	});
	System.out.println(ai.get());
	TimeUtil.println("decode : ");
    }

    @Test
    public void test_query() {
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQStore store = FileQMStore.of(topic, fileIndexer);
	long offset = 0L;
	TimeUtil.record();
	AtomicInteger ai = new AtomicInteger();

	while (true) {
	    QConsume l = store.queryForRaw(QQuery.of(topic, offset));
	    l.foreachMessageData((list) -> {
		ai.addAndGet(list.length);
	    });
	    // List<QMessage<?, ?>> t = l.toMessageData();
	    // System.out.println("size :" + t.size() + " offset :" +
	    // l.getOffset());
	    System.out.println("query offset : " + offset  + " ret offset : " + l.getO());
	    if (offset == l.getO()) {
		break;
	    }
	    offset = l.getO();
	}
	TimeUtil.println("decode : ");
	System.out.println(ai.get());
    }

    @Test
    public void test_shareFile() throws Exception {
	String path = "e:/testshare";
	FileUtil.createDirs(path);
	PrintWriter writer = new PrintWriter(new FileWriter(path, true));
	LineNumberReader reader = new LineNumberReader(new FileReader(path), 1024 * 8);

	Thread w = new Thread(new Runnable() {

	    @Override
	    public void run() {
		int i = 0;
		long start = System.currentTimeMillis();
		while (true) {
		    String line = "xxxx:" + (i++);
		    writer.println(line);
		    if ((System.currentTimeMillis() - start) > 2000) {
			writer.flush();
			start = System.currentTimeMillis();
		    }
		    try {
			Thread.sleep(200);
		    } catch (InterruptedException e) {
		    }
		}

	    }
	});
	w.setDaemon(true);
	w.start();

	Thread r = new Thread(new Runnable() {

	    @Override
	    public void run() {
		while (true) {
		    String line = null;
		    try {
			while ((line = reader.readLine()) != null) {
			    System.out.println(line);
			}
			System.out.println("end");
		    } catch (IOException e1) {
			e1.printStackTrace();
		    }
		    try {
			Thread.sleep(500);
		    } catch (InterruptedException e) {
		    }
		}

	    }
	});
	r.setDaemon(true);
	r.start();
	r.join();
	w.join();
    }

}
