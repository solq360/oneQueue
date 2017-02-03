package com.eyu.onequeue;

public interface QMServerConfig {
    // MESSAGE
    final short MESSAGE_CODE_NORMAL = 0;
    final short MESSAGE_CODE_ZIP = 1;

    int MESSAGE_ZIP_VALUE_UPPER = 500;

    // STORE
    /** 消息 文件大小 **/
    final long STORE_SPLIT_SIZE = 1024 * 1024 * 64;
    /** 文件保存根文件，支持迁移 **/
    final String STORE_ROOT_PATH = "e:/qmdata/";
    /** 自动关闭文件资源时间 间隔 **/
    final long STORE_FILE_CLOSE_INTERVAL = 1000 * 60 * 5;
    /** 持久化文件时间 间隔 **/
    final long STORE_FILE_PERSIST_INTERVAL = 1000 * 10;
    
    /** 消息缓冲队列开关**/
    final boolean STORE_QUEUE_OPEN = true;
    /** 消息缓冲队列初始化长度 根据业务每秒支持量设置 **/
    final int STORE_QUEUE_BUFFER_SIZE = 9000;
    /** 写入文件 队列边界 **/
    final int STORE_QUEUE_PERSIST_SIZE = (int) (STORE_QUEUE_BUFFER_SIZE * 0.9);
    /** 查询返回数据最大值 **/
	final long STORE_QUEUE_MAX_SIZE = 1024 * 1024 * 2;

    //PUSH    
	/**推送时间隔**/
    final int PUSH_MESSAGE_INTERVAL = 100;
    /**推送保存记录文件**/
	final String PUSH_PERSIST_FILE = "e:/qmdata/pushRecord";
	/**多少分钟持久分推送记录**/
	final long PUSH_PERSIST_INTERVAL = 1000*60*5;


    static String getStoreRootPath(String topic) {
	return STORE_ROOT_PATH + topic + "/";
    }

    static String getStoreFilePath(String topic, long fileNum) {
	return getStoreRootPath(topic) + fileNum + "-" + topic;
    }

}
