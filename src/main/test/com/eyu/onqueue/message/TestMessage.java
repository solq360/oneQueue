package com.eyu.onqueue.message;

import org.junit.Test;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author solq
 */
public class TestMessage {

    QProduce ofProduce() {
	TestMessageObject obj = TestMessageObject.of();
	QMessage<Long, TestMessageObject> q1 = QMessage.of(1L, obj);
	QMessage<Long, TestMessageObject> q2 = QMessage.of(1L, obj);
	QMessage<Long, TestMessageObject> q3 = QMessage.of(1L, obj);
	QProduce qm = QProduce.of("topic_test", q1, q2, q3);
	return qm;
    }

    @Test
    public void pack_serial() {
	// 应用层处理
	QMessage<Long, Long> q1 = QMessage.of(1L, 1L);
	QMessage<Long, Long> q2 = QMessage.of(1L, 1L);
	QMessage<Long, Long> q3 = QMessage.of(1L, 1L);

	// 系统层处理
	QProduce qm = QProduce.of("topic_test", q1, q2, q3);

	// 测试JSON
	String json = SerialUtil.writeValueAsString(qm);
	System.out.println(json);

	// 通信层处理
	byte[] body = SerialUtil.writeValueAsBytes(qm);
	System.out.println("没有压缩的大小 : " + body.length);

	byte[] zbody = SerialUtil.writeValueAsZipBytes(qm);
	System.out.println("压缩的大小 : " + zbody.length);

	short s = 0;
	QPacket qp = QPacket.of(s, 5000, 1, body);

    }

    @Test
    public void pack_unSerial() {
	QProduce qm = ofProduce();

	byte[] body = SerialUtil.writeValueAsBytes(qm);
	byte[] zbody = SerialUtil.writeValueAsZipBytes(qm);

	qm = SerialUtil.readValue(body, QProduce.class);
	qm = SerialUtil.readZipValue(zbody, QProduce.class);
	QMessage<?, ?>[] mList = qm.getB();
	TypeReference<TestMessageObject> tr = new TypeReference<TestMessageObject>() {
	};
	for (QMessage<?, ?> m : mList) {
	    System.out.println(m.getB());
	    System.out.println(m.formatMessage(tr).getStart());
	}
    }

    @Test
    public void pack_to_byte() {
	QProduce qm = ofProduce();
	byte[] body = SerialUtil.writeValueAsZipBytes(qm);

	short s = 1;
	QPacket qp = QPacket.of(s, 1234567891011L, 123456789101112L, body);
	System.out.println("QPacket : " + qp.getSn());

	byte[] bytes = qp.toBytes();
	System.out.println("QPacket to bytes : " + bytes.length);

	qp = QPacket.byte2Package(bytes);
	System.out.println("bytes to QPacket : " + qp.getC());
	System.out.println("bytes to QPacket : " + qp.getSn());
	System.out.println("bytes to QPacket : " + qp.getSid());

    }
}
