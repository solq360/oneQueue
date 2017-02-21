package com.eyu.onequeue.rpc;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.eyu.onequeue.message.TestMessageObject;
import com.eyu.onequeue.rpc.service.QRpcFactory;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

public class TestEnhanceImpl implements ITestEnhance {

    @Override
    public void a(double a) {
	System.out.println("a接收到 数值 : " + a);

    }

    @Override
    public void b(float a) {
	System.out.println("b接收到 数值 : " + a);

    }

    @Override
    public void c(byte a) {
	System.out.println("c接收到 数值 : " + a);
    }

    @Override
    public void d(long a) {
	System.out.println("d接收到 数值 : " + a);
    }

    @Override
    public void e(TestMessageObject obj) {
	if (obj == null) {
	    return;
	}
	System.out.println("e接收到 数值 : " + SerialUtil.writeValueAsString(obj));

    }

    @Override
    public void f(AtomicLong a) {
	System.out.println("f接收到 数值 : " + a.get());
    }

    @Override
    public TestMessageObject g(AtomicLong a, long b, TestMessageObject c) {
	System.out.println("g接收到 数值 : " + a.get());
	System.out.println("g接收到 数值 : " + b);
	System.out.println("g接收到 数值 : " + SerialUtil.writeValueAsString(c));
 	return c;
    }
    public static void main(String[] args) {
	ITestEnhance proxyObject = QRpcFactory.loadProxy(ITestEnhance.class,1L);
	QRpcFactory.registerInvokeService(new TestEnhanceImpl());

	proxyObject.a(-0.2);
	proxyObject.b(0);
	proxyObject.c((byte) 0x0);
	proxyObject.c((byte) 0x1);
	proxyObject.d((byte) 0x1);

	proxyObject.e(TestMessageObject.of());
	proxyObject.f(new AtomicLong(-9));

	proxyObject.e(null);
	TestMessageObject obj=proxyObject.g(new AtomicLong(-11112), Long.MAX_VALUE, TestMessageObject.of());
	SerialUtil.println(obj);
    }

    @Test
    public void testNumParse() {
	Number v = -PacketUtil.BIT_8;
	Number retValue = null;
	byte[] ret = new byte[1];
	PacketUtil.writeByte(0, v.byteValue(), ret);
	retValue = Short.valueOf(PacketUtil.readByte(0, ret));
	System.out.println(retValue);

	v = PacketUtil.BIT_16;
	ret = new byte[2];
	PacketUtil.writeShort(0, v.shortValue(), ret);
	retValue = Short.valueOf(PacketUtil.readShort(0, ret));
	System.out.println(retValue);

	v = 0.2d;
	ret = new byte[8];
	PacketUtil.writeDouble(0, v.doubleValue(), ret);
	retValue = PacketUtil.readDouble(0, ret);
	System.out.println(retValue);
    }
}
