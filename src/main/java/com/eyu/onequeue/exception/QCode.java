package com.eyu.onequeue.exception;

import com.eyu.onequeue.reflect.anno.Des;

/**
 * @author solq
 * **/
public interface QCode {
    @Des("完成")
    public final short SUCCEED = 0;
    @Des("处理失败，未知错误")
    public final short ERROR_UNKNOWN = -1;
    @Des("对方没有及时回应业务消息,超时")
    public final short MESSAGE_ERROR_TIMEOUT = toCode(1, 0);
    @Des("json编码失败")
    public final short JSON_ERROR_DECODE = toCode(2, 0);
    
    @Des("socket连接出错")
    public final short SOCKET_ERROR_CONNECT = toCode(3, 0);
    @Des("socket连接超时")
    public final short SOCKET_ERROR_CONNECT_TIMEOUT = toCode(3, 1);

    /**
     * 防止冲突，以模块号区分
     **/
    public static short toCode(int m, int code) {
	Integer ret = m * 50 + code;
	return ret.shortValue();
    }

//    public static short fromModel(short code) {
//	Integer ret = (code -X) / 50  ;
//	return ret.shortValue();
//    }
}
