package com.eyu.onequeue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.eyu.onequeue.protocol.model.QSubscribe;
import com.eyu.onequeue.reflect.PropertiesFactory;
import com.eyu.onequeue.reflect.anno.FieldValue;
import com.eyu.onequeue.socket.model.NettyClientConfig;
import com.eyu.onequeue.socket.model.NettyServerConfig;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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

    // other

    /** 启动服务模式 影响 记录存储目录 **/
    public boolean SERVER_MODEL = true;
    /** 集群服务器 <address> client端不能为NULL **/
    @FieldValue("QM.CLUSTER_LIST")
    /** 本地名称，多应用不能相同，注册时用到 client端不能为NULL **/
    public String LOCALNAME;
    /** 集群服务器 <address> client端不能为NULL **/
    @FieldValue("QM.CLUSTER_LIST")
    public Set<String> CLUSTER_LIST;
    /** 订阅信息 topic : groupId 填写开启 **/
    @FieldValue("QM.TOPIC_INFO")
    public Set<String> TOPIC_INFO;
    /** 日志保存路径 <订阅,路径> consume端不能为NULL **/
    @FieldValue("QM.CONSUME_LOG_SAVE_DIRS")
    public Map<String, String> CONSUME_LOG_SAVE_DIRS = new HashMap<>(1);

    public Collection<QSubscribe> getTopics() {
	Collection<QSubscribe> ret = new HashSet<>();
	if (TOPIC_INFO == null) {
	    return ret;
	}
	for (String str : TOPIC_INFO) {
	    String[] s = str.split(":");
	    ret.add(QSubscribe.of(s[0], s[1]));
	}
	return ret;
    }

    // NETTY
    /** server port **/
    @FieldValue("QM.NETTY_SERVER_PORT")
    public int NETTY_SERVER_PORT = 8080;
    /** server bossgroup **/
    @FieldValue("QM.NETTY_SERVER_BOSSGROUP")
    public int NETTY_SERVER_BOSSGROUP = 1;
    /** server workergroup **/
    @FieldValue("QM.NETTY_SERVER_WORKERGROUP")
    public int NETTY_SERVER_WORKERGROUP = 4;
    /** server socket option **/
    @FieldValue("QM.NETTY_SERVER_SESSION_OPTION")
    public Map<String, ?> NETTY_SERVER_SESSION_OPTION;
    /** child socket option **/
    @FieldValue("QM.NETTY_SERVER_CHILD_SESSION_OPTION")
    public Map<String, ?> NETTY_SERVER_CHILD_SESSION_OPTION;
    /** 处理模型实现 **/
    @FieldValue("QM.NETTY_SERVER_ACCEPTOR")
    public Class<? extends ServerSocketChannel> NETTY_SERVER_ACCEPTOR = NioServerSocketChannel.class;

    /** 发送消息请求超时 */
    @FieldValue("QM.NETTY_MESSAGE_REQUEST_TIMEOUT")
    public long NETTY_MESSAGE_REQUEST_TIMEOUT = 1000 * 30;
    /** 消息回调清理时间隔 **/
    @FieldValue("QM.NETTY_MESSAGE_CALLBACK_CLEAR_INTERVAL")
    public int NETTY_MESSAGE_CALLBACK_CLEAR_INTERVAL = 1000 * 40;

    // client
    /** client workergroup **/
    @FieldValue("QM.NETTY_CLIENT_WORKERGROUP")
    public int NETTY_CLIENT_WORKERGROUP = 4;

    /** socket连接超时 **/
    @FieldValue("QM.NETTY_CLIENT_CONNECT_TIMEOUT")
    public long NETTY_CLIENT_CONNECT_TIMEOUT = 666;
    /** client socket option **/
    @FieldValue("QM.NETTY_CLIENT_SESSION_OPTION")
    public Map<String, ?> NETTY_CLIENT_SESSION_OPTION;
    /** 处理模型实现 **/
    @FieldValue("QM.NETTY_CLIENT_ACCEPTOR")
    public Class<? extends SocketChannel> NETTY_CLIENT_ACCEPTOR = NioSocketChannel.class;

    public NettyServerConfig buildServerConfig() {
	Map<ChannelOption<?>, Object> netty_server_session_option = convertOptions(NETTY_SERVER_SESSION_OPTION);
	Map<ChannelOption<?>, Object> netty_server_child_session_option = convertOptions(NETTY_SERVER_CHILD_SESSION_OPTION);

	return NettyServerConfig.of(NETTY_SERVER_PORT, NETTY_SERVER_BOSSGROUP, NETTY_SERVER_WORKERGROUP, netty_server_session_option, netty_server_child_session_option, NETTY_SERVER_ACCEPTOR);
    }

    public NettyClientConfig buildClientConfig() {
	Map<ChannelOption<?>, Object> netty_server_child_session_option = convertOptions(NETTY_SERVER_CHILD_SESSION_OPTION);

	return NettyClientConfig.of(NETTY_CLIENT_CONNECT_TIMEOUT, NETTY_CLIENT_WORKERGROUP, netty_server_child_session_option, NETTY_CLIENT_ACCEPTOR);
    }

    /** 转换换netty socket 参数 因为 ChannelOption id 是内部自增，所有要拿到原来的静态属性 **/
    private static Map<ChannelOption<?>, Object> convertOptions(Map<String, ?> configs) {
	if (configs == null) {
	    return null;
	}
	Map<ChannelOption<?>, Object> ret = new HashMap<>(configs.size());

	configs.forEach((k, v) -> {
	    Field f = null;
	    try {
		f = ChannelOption.class.getDeclaredField(k);
	    } catch (Exception e) {
	    }
	    if (f == null) {
		return;
	    }
	    if ((f.getModifiers() & Modifier.FINAL) == 0) {
		return;
	    }
	    if ((f.getModifiers() & Modifier.STATIC) == 0) {
		return;
	    }
	    if (!TypeUtils.isAssignable(f.getType(), ChannelOption.class)) {
		return;
	    }
	    f.setAccessible(true);
	    try {
		ChannelOption<?> key = (ChannelOption<?>) f.get(null);
		ret.put(key, v);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	});
	return ret;
    }

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
    /** server 文件保存根文件，支持迁移 **/
    @FieldValue("QM.STORE_SERVER_ROOT_PATH")
    public String STORE_SERVER_ROOT_PATH = "e:/qmdata/server";
    /** client 文件保存根文件，支持迁移 **/
    public String STORE_CLIENT_ROOT_PATH = "e:/qmdata/client";

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
    public int STORE_QUEUE_BUFFER_SIZE = 5000;
    /** 写入文件 队列边界 **/
    @FieldValue("QM.STORE_QUEUE_PERSIST_SIZE")
    public double STORE_QUEUE_PERSIST_SIZE = 0.9;
    /** 查询返回数据最大值 **/
    @FieldValue("QM.STORE_QUEUE_MAX_SIZE")
    public long STORE_QUEUE_MAX_SIZE = 1024 * 1024 * 1;

    /** 消息仓库根目录 **/
    public String getStoreRootPath(String topic) {
	return (SERVER_MODEL ? STORE_SERVER_ROOT_PATH : STORE_CLIENT_ROOT_PATH) + topic + "/";
    }

    /** 生成topic目录 **/
    public String buildStoreFilePath(String topic, long fileNum) {
	return getStoreRootPath(topic) + fileNum + "-" + topic;
    }

    // PUSH
    /** 推送时间隔 **/
    @FieldValue("QM.PUSH_MESSAGE_INTERVAL")
    public int PUSH_MESSAGE_INTERVAL = 500;
    /** server 推送保存记录文件 **/
    @FieldValue("QM.PUSH_PERSIST_FILE")
    public String PUSH_PERSIST_SERVER_FILE = "e:/qmdata/pushRecord/server";
    /** client 推送保存记录文件 **/
    @FieldValue("QM.PUSH_PERSIST_FILE")
    public String PUSH_PERSIST_CLIENT_FILE = "e:/qmdata/pushRecord/client";
    /** 多少分钟持久分推送记录 **/
    @FieldValue("QM.PUSH_PERSIST_INTERVAL")
    public long PUSH_PERSIST_INTERVAL = 1000 * 60 * 5;

    public String getPushPersistPath() {
	return (SERVER_MODEL ? PUSH_PERSIST_SERVER_FILE : PUSH_PERSIST_CLIENT_FILE);
    }

}
