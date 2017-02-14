package com.eyu.onequeue.exception;

import com.eyu.onequeue.reflect.anno.Des;

/**
 * @author solq
 **/
public interface QCode {
    final short RANGE = 30;

    public final short MODEL_MESSAGE = 1;
    public final short MODEL_JSON = 2;
    public final short MODEL_SOCKET = 3;

    @Des("完成")
    public final short SUCCEED = 0;
    @Des("处理失败，未知错误")
    public final short ERROR_UNKNOWN = -1;
    @Des("对方没有及时回应业务消息,超时")
    public final short MESSAGE_ERROR_TIMEOUT = toCode(MODEL_MESSAGE, 1);
    @Des("对方没有及时回应业务消息,回收")
    public final short MESSAGE_ERROR_RECYCLE = toCode(MODEL_MESSAGE, 2);

    @Des("json编码失败")
    public final short JSON_ERROR_DECODE = toCode(MODEL_JSON, 1);
    @Des("unzip失败")
    public final short UNZIP_ERROR = toCode(MODEL_JSON, 2);
    @Des("zip失败")
    public final short ZIP_ERROR = toCode(MODEL_JSON, 3);

    @Des("socket连接出错")
    public final short SOCKET_ERROR_CONNECT = toCode(MODEL_SOCKET, 1);
    @Des("socket连接超时")
    public final short SOCKET_ERROR_CONNECT_TIMEOUT = toCode(MODEL_SOCKET, 2);
    @Des("socket请求超时")
    public final short SOCKET_ERROR_REQUEST_TIMEOUT = toCode(MODEL_SOCKET, 3);
    @Des("没有绑定session")
    public final short SOCKET_ERROR_UNBIND_SESSION = toCode(MODEL_SOCKET, 4);
    @Des("已存在session")
    public final short SOCKET_ERROR_ALREADY_SESSION = toCode(MODEL_SOCKET, 4);
    @Des("session id 与 packet sid 不一致")
    public final short SOCKET_ERROR_NOEQ_SESSION = toCode(MODEL_SOCKET, 5);
    @Des("未知操作")
    public final short SOCKET_UNKNOWN_OPCODE = toCode(MODEL_SOCKET, 6);
    @Des("解码失败")
    public final short SOCKET_ERROR_DECODE = toCode(MODEL_SOCKET, 7);
    @Des("编码失败")
    public final short SOCKET_ERROR_ENCODE = toCode(MODEL_SOCKET, 8);
    @Des("包头标识不对")
    public final short SOCKET_ERROR_PCKET_FLAG = toCode(MODEL_SOCKET, 9);

    /**
     * 防止冲突，以模块号区分
     **/
    public static short toCode(int m, int code) {
	Integer ret = m * RANGE + code;
	return ret.shortValue();
    }

    public static boolean checkModel(short code, int m) {
	if (code >= toCode(m, 1) && code < toCode(m + 1, 1)) {
	    return true;
	}
	return false;
    }

}
