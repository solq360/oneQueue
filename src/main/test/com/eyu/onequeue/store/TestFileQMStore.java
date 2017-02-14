package com.eyu.onequeue.store;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eyu.onequeue.message.TestMessageObject;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.store.model.IQStore;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.service.FileIndexer;
import com.eyu.onequeue.store.service.FileQMStore;
import com.eyu.onequeue.util.TimeUtil;

public class TestFileQMStore {
    String topic = "topic_test";

    QProduce ofProduce() {
	int count = 2000;
	TestMessageObject obj = TestMessageObject.ofBig(200);
	List<QMessage<Long, TestMessageObject>> tmp = new ArrayList<>();
	for (int i = 0; i < count; i++) {
	    tmp.add(QMessage.of(1L, obj));
	}
	QProduce qm = QProduce.of(topic, tmp.toArray(new QMessage[count]));
	return qm;
    }

    @Test
    public void test_frame() {
	QProduce qproduce = ofProduce();

	String topic = qproduce.getT();
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQStore store = FileQMStore.of(topic, fileIndexer);

	long start = System.currentTimeMillis();
	for (int i = 0; i < 50; i++) {
	    store.save(qproduce.getB());
	}
	store.close();
	long end = System.currentTimeMillis();
	System.out.println("persist : " + (end - start));
    }

    @Test
    public void test_query() {
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQStore store = FileQMStore.of(topic, fileIndexer);
	TimeUtil.record();

	QConsume l = store.query(QQuery.of(topic, 0));
	TimeUtil.println("query : ");
	TimeUtil.record();

	List<QMessage<?, ?>> t = l.toMessageData();
	System.out.println(t.size());
	TimeUtil.println("decode : ");

    }

    @Test
    public void test_decodeRaw() {
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQStore store = FileQMStore.of(topic, fileIndexer);

	QConsume l = store.query(QQuery.of(topic, 0));

	TimeUtil.record();
	l.foreachMessageData((list) -> {
	    System.out.println(list.length);
	});
	TimeUtil.println("decode : ");
    }

}