package com.eyu.onequeue.message;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author solq
 */
public class TestMessage {
    private static String topic = "xxx";

    public static QProduce ofProduce(String topic, int count, int attrSize) {
	TestMessageObject obj = TestMessageObject.ofBig(attrSize);
	List<Object> tmp = new ArrayList<>();
	for (int i = 0; i < count; i++) {
	    tmp.add(obj);
	}
	QProduce qm = QProduce.of(topic, tmp.toArray(new Object[count]));

	return qm;
    }

    @Test
    public void pack_serial() {
	// 应用层处理
	// 系统层处理
	QProduce qm = TestMessage.ofProduce(topic, 5, 0);
	// 测试JSON
	String json = SerialUtil.writeValueAsString(qm);
	System.out.println(json);

	// 通信层处理
	byte[] body = SerialUtil.writeValueAsBytes(qm);
	System.out.println("没有压缩的大小 : " + body.length);

	byte[] zbody = SerialUtil.writeValueAsZipBytes(qm);
	System.out.println("压缩的大小 : " + zbody.length);
	byte c = 1;
	QPacket qp = QPacket.of(c, 5000, 1, null, body);

    }

    @Test
    public void pack_unSerial() {
	QProduce qm = TestMessage.ofProduce(topic, 5, 0);

	byte[] body = SerialUtil.writeValueAsBytes(qm);
	byte[] zbody = SerialUtil.writeValueAsZipBytes(qm);

	qm = SerialUtil.readValue(body, QProduce.class);
	qm = SerialUtil.readZipValue(zbody, QProduce.class);
	Object[] mList = qm.getB();
	TypeReference<TestMessageObject> tr = new TypeReference<TestMessageObject>() {
	};
	for (Object m : mList) {
	    System.out.println(m.toString());
	    System.out.println(SerialUtil.formatMessage(m, tr).getStart());
	}
    }

    @Test
    public void pack_to_byte() {
	QProduce qm = TestMessage.ofProduce(topic, 5, 0);
	byte[] body = SerialUtil.writeValueAsZipBytes(qm);

	byte c = 1;
	QPacket qp = QPacket.of(c, 1234567891011L, 123456789101112L, null, body);
	System.out.println("QPacket : " + qp.getSn());

	byte[] bytes = qp.toBytes();
	System.out.println("QPacket to bytes : " + bytes.length);

	qp = QPacket.of(bytes);
	System.out.println("bytes to QPacket : " + qp.getC());
	System.out.println("bytes to QPacket : " + qp.getSn());
	System.out.println("bytes to QPacket : " + qp.getSid());

    }
}
