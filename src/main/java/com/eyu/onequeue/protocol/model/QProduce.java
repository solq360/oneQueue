package com.eyu.onequeue.protocol.model;

/***
 * 属性名采取最少字母命名，减少通信跟存储 生产消息对象
 * 
 * @author solq
 */
public class QProduce {
    /** 订阅 **/
    private String t;
    /** 内容信息 **/
    private QMessage<?, ?>[] b;

    // getter

    public QMessage<?, ?>[] getB() {
	return b;
    }

    public String getT() {
	return t;
    }

    public static QProduce of(String topic, QMessage<?, ?>... qmessages) {
	QProduce ret = new QProduce();
	ret.t = topic;
 	ret.b = qmessages;
	return ret;
    }

}
