package com.eyu.onequeue.rpc.model;
/**
 * @author solq  
 */
public interface IRpcReceive {
    public <T> T receive(byte command, Object[] args);
}
