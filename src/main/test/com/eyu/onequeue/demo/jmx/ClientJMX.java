package com.eyu.onequeue.demo.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.eyu.onequeue.demo.jmx.model.ITestMBean;

public class ClientJMX {

    public static void main(String[] args) throws Exception {
	//添加连接账号密码
	Map<String, Object> prop = new HashMap<String, Object>();
	prop.put(JMXConnector.CREDENTIALS, new String[] { "userName", "password" });
	//创建JMX连接
	JMXConnector conn = JMXConnectorFactory.connect(new JMXServiceURL(ServerJMX.URL), prop);
	//生成MEean proxy 跟 rmi 一样使用，不过 jmx传输数据类型太少，本人测试只支持java基本类型
 	ITestMBean obj = JMX.newMBeanProxy(conn.getMBeanServerConnection(),  new ObjectName(ServerJMX.NAME), ITestMBean.class);
	int ret = obj.b(12);
	System.out.println(ret);
    }
}