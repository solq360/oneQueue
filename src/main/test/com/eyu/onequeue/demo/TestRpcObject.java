package com.eyu.onequeue.demo;

import com.eyu.onequeue.rpc.anno.QCommond;
import com.eyu.onequeue.rpc.anno.QModel;

@QModel(1)
public interface TestRpcObject {
    @QCommond(1)
    public void a();
}
