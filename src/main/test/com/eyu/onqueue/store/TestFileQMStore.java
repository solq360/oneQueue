package com.eyu.onqueue.store;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.store.FileIndexer;
import com.eyu.onequeue.store.FileQMStore;
import com.eyu.onequeue.store.model.IQMStore;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;
import com.eyu.onequeue.util.SerialUtil;
import com.eyu.onqueue.message.TestMessageObject;

public class TestFileQMStore {

    QProduce ofProduce() {
	TestMessageObject obj = TestMessageObject.of();
	int count = 2000;
	List<QMessage<Long, TestMessageObject>> tmp = new ArrayList<>();
	for (int i = 0; i < count; i++) {
	    tmp.add(QMessage.of(1L, obj));
	}
	QProduce qm = QProduce.of("topic_test", tmp.toArray(new QMessage[count]));
	return qm;
    }

    @Test
    public void test_frame() {
	QProduce qproduce = ofProduce();

	String topic = qproduce.getT();
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQMStore store = FileQMStore.of(topic, fileIndexer);

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
	String topic = "topic_test";
	// 文件物理与业务逻辑层处理
	FileIndexer fileIndexer = FileIndexer.of(topic);
	// 应用层处理
	IQMStore store = FileQMStore.of(topic, fileIndexer);

	QResult l = store.query(QQuery.of(0));
	for (byte[] b : l.getBatchData()) {
	    List<QMessage> t = SerialUtil.readArray(SerialUtil.unZip(b), QMessage.class);
	    System.out.println(t.size());
	}
     }
}