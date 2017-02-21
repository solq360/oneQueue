package com.eyu.onequeue.rpc.model;
/**
 * @author solq  
 * @version 2014-3-6 上午11:20:38
 */
public interface IRpcContext {
    RpcContext getContext();
    void setContext(RpcContext ctx);
}
