package com.eyu.onequeue.rpc;

import com.eyu.onequeue.rpc.anno.QCommond;

public interface TestEnhance {
    @QCommond(1)
    public void a(int a, String b);
    public void b(int a, String b);
}