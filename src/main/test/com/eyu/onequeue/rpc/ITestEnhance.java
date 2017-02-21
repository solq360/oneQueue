package com.eyu.onequeue.rpc;

import java.util.concurrent.atomic.AtomicLong;

import com.eyu.onequeue.message.TestMessageObject;
import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;

@QModel(21)
public interface ITestEnhance {
    @QCommond(1)
    public void a(double a);

    @QCommond(2)
    public void b(float a);

    @QCommond(3)
    public void c(byte a);

    @QCommond(4)
    public void d(long a);

    @QCommond(5)
    public void e(TestMessageObject obj);

    @QCommond(6)
    public void f(AtomicLong al);
    
    @QCommond(7)
    public TestMessageObject g(AtomicLong a,long b,TestMessageObject c);
}