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
    public final short MODEL_ENHANCE_PROXY = 4;

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
    @Des("增强代码失败")
    public final short ENHANCE_ERROR = toCode(MODEL_ENHANCE_PROXY, 1);
    @Des("rpc不支持参数类型")
    public final short ENHANCE_ERROR_RPC_NONSUPPORT_TYPE = toCode(MODEL_ENHANCE_PROXY, 2);
    @Des("rpc调用编码出错")
    public final short ENHANCE_ERROR_RPC_INVOKE = toCode(MODEL_ENHANCE_PROXY, 3);
    @Des("rpc发送编码出错")
    public final short ENHANCE_ERROR_RPC_SEND = toCode(MODEL_ENHANCE_PROXY, 4);
    @Des("rpc参数不允许为NULL")
    public final short ENHANCE_ERROR_RPC_PARAM_EMPTY = toCode(MODEL_ENHANCE_PROXY, 5);
    @Des("rpc未找到调用model")
    public final short ENHANCE_ERROR_RPC_NOFIND_MODEL = toCode(MODEL_ENHANCE_PROXY,6);
    @Des("rpc参数非法")
    public final short ENHANCE_ERROR_RPC_PARAM_ILLEGITMACY = toCode(MODEL_ENHANCE_PROXY,7);
    @Des("rpc没有调用服务")
    public final short ENHANCE_ERROR_RPC_NOFIDN_SERVICE = toCode(MODEL_ENHANCE_PROXY,8);
    
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
