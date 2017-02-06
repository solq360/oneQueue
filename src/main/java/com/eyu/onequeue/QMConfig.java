package com.eyu.onequeue;

import java.util.Map;

import com.eyu.onequeue.reflect.PropertiesFactory;
import com.eyu.onequeue.reflect.anno.FieldValue;
import com.eyu.onequeue.socket.model.NettyClientConfig;
import com.eyu.onequeue.socket.model.NettyServerConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/***
 * qm config 所有配置 属性不能加 final 否则 jvm 会优化
 * 
 * @author solq
 */
public class QMConfig {
    private static QMConfig instance = null;

    public static QMConfig getInstance() {
	if (instance != null) {
	    return instance;
	}
	synchronized (QMConfig.class) {
	    if (instance != null) {
		return instance;
	    }
	    instance = PropertiesFactory.initField(QMConfig.class, "qm.properties");
	}
	return instance;
    }

    // NETTY
    /** server启动阻塞 **/
    @FieldValue("QM.NETTY_SERVER_OPEN_START_BLOCK")
    public boolean NETTY_SERVER_OPEN_START_BLOCK = false;
    @FieldValue("QM.NETTY_SERVER_PORT")
    public int NETTY_SERVER_PORT = 8080;

    @FieldValue("QM.NETTY_SERVER_BOSSGROUP")
    public int NETTY_SERVER_BOSSGROUP = 1;
    @FieldValue("QM.NETTY_SERVER_WORKERGROUP")
    public int NETTY_SERVER_WORKERGROUP = 4;

    @FieldValue("QM.NETTY_SERVER_SESSIONO_PTIONS")
    public Map<ChannelOption<?>, ?> NETTY_SERVER_SESSIONO_PTIONS;

    @FieldValue("QM.NETTY_SERVER_ACCEPTOR")
    public Class<? extends ServerSocketChannel> NETTY_SERVER_ACCEPTOR = NioServerSocketChannel.class;

    public NettyServerConfig buildServerConfig() {
	return NettyServerConfig.of(NETTY_SERVER_PORT, NETTY_SERVER_OPEN_START_BLOCK, NETTY_SERVER_BOSSGROUP, NETTY_SERVER_WORKERGROUP, NETTY_SERVER_SESSIONO_PTIONS, NETTY_SERVER_ACCEPTOR);
    }
    public NettyClientConfig buildClientConfig() {
  	// TODO Auto-generated method stub
  	return null;
      }
    // MESSAGE
    /** 消息回调清理时间隔 **/
    @FieldValue("QM.MESSAGE_CALLBACK_CLEAR_INTERVAL")
    public int MESSAGE_CALLBACK_CLEAR_INTERVAL = 1000 * 60 * 2;
    // PACKET
    /** 包开头标志 **/
    @FieldValue("QM.PACKET_HEAD_FLAG")
    public short PACKET_HEAD_FLAG = 12;

    /** 包结束标志 **/
    @FieldValue("QM.PACKET_END_FLAG")
    public byte PACKET_END_FLAG = 0x5;

    /** 包大小上限 **/
    @FieldValue("QM.PACKET_MAX_LENGTH")
    public int PACKET_MAX_LENGTH = 1024 * 1024 * 6;

    /** 添加复杂度，防止逆向分析包 */
    public short getPacketHeadFlag(int len) {
	Integer ret = PACKET_HEAD_FLAG | len % (Short.MAX_VALUE - PACKET_HEAD_FLAG - 1);
	return ret.shortValue();
    }

    /** 添加复杂度，防止逆向分析包 */
    public byte getPacketEndFlag(int len) {
	Integer ret = PACKET_END_FLAG | len % (Short.MAX_VALUE - PACKET_END_FLAG - 1);
	return ret.byteValue();
    }

    // POOL
    /** store 线程池核心数 **/
    @FieldValue("QM.POOL_STORE_CORE")
    public int POOL_STORE_CORE = Math.max(8, Runtime.getRuntime().availableProcessors());
    /** 推送线程池核心数 **/
    @FieldValue("QM.POOL_PUSH_CORE")
    public int POOL_PUSH_CORE = 8;
    /** 接收消息业务线程池核心数 **/
    @FieldValue("QM.POOL_REQUEST_CORE")
    public int POOL_REQUEST_CORE = Math.min(128, Runtime.getRuntime().availableProcessors() * 8);
    /** 清理过期消息线程池核心数 **/
    @FieldValue("QM.POOL_REQUEST_CORE")
    public int POOL_CLEAR_MESSAGE_CORE = Math.min(8, Runtime.getRuntime().availableProcessors());
    
    // STORE
    /** 消息 文件大小 经测试千W级别数据量才占几M空间，所以不用设置太大 **/
    @FieldValue("QM.STORE_SPLIT_SIZE")
    public long STORE_SPLIT_SIZE = 1024 * 1024 * 32;
    /** 文件保存根文件，支持迁移 **/
    @FieldValue("QM.STORE_ROOT_PATH")
    public String STORE_ROOT_PATH = "e:/qmdata/";

    /** 持久化文件时间 间隔 **/
    @FieldValue("QM.STORE_FILE_PERSIST_INTERVAL")
    public long STORE_FILE_PERSIST_INTERVAL = 1000 * 10;
    /** 自动关闭文件资源时间 间隔 **/
    @FieldValue("QM.STORE_FILE_CLOSE_INTERVAL")
    public long STORE_FILE_CLOSE_INTERVAL = 1000 * 60 * 5;
    /** 删除文件资源时间 间隔 **/
    @FieldValue("QM.STORE_FILE_DELETE_INTERVAL")
    public long STORE_FILE_DELETE_INTERVAL = 1000 * 60 * 60 * 24 * 15;

    /** 消息缓冲队列开关 **/
    @FieldValue("QM.STORE_QUEUE_OPEN")
    public boolean STORE_QUEUE_OPEN = true;
    /** 消息缓冲队列初始化长度 根据业务每秒支持量设置，不能设置太高，原因解码时占大量CPU时间跟释放大量临时内存 **/
    @FieldValue("QM.STORE_QUEUE_BUFFER_SIZE")
    public int STORE_QUEUE_BUFFER_SIZE = 9000;
    /** 写入文件 队列边界 **/
    @FieldValue("QM.STORE_QUEUE_PERSIST_SIZE")
    public double STORE_QUEUE_PERSIST_SIZE = 0.9;
    /** 查询返回数据最大值 **/
    @FieldValue("QM.STORE_QUEUE_MAX_SIZE")
    public long STORE_QUEUE_MAX_SIZE = 1024 * 1024 * 2;

    public String getStoreRootPath(String topic) {
	return STORE_ROOT_PATH + topic + "/";
    }

    public String getStoreFilePath(String topic, long fileNum) {
	return getStoreRootPath(topic) + fileNum + "-" + topic;
    }

    // PUSH
    /** 推送时间隔 **/
    @FieldValue("QM.PUSH_MESSAGE_INTERVAL")
    public int PUSH_MESSAGE_INTERVAL = 100;
    /** 推送保存记录文件 **/
    @FieldValue("QM.PUSH_PERSIST_FILE")
    public String PUSH_PERSIST_FILE = "e:/qmdata/pushRecord";
    /** 多少分钟持久分推送记录 **/
    @FieldValue("QM.PUSH_PERSIST_INTERVAL")
    public long PUSH_PERSIST_INTERVAL = 1000 * 60 * 5;

  

}
