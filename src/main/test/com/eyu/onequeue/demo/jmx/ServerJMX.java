package com.eyu.onequeue.demo.jmx;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import com.eyu.onequeue.demo.jmx.model.TestMBeanServiceImpl;

public class ServerJMX {
    public final static String NAME = "ITestMBean:name=Test";
    public final static String URL = "service:jmx:rmi:///jndi/rmi://localhost:8989/Test";

    public static void main(String[] args) throws Exception {
	LocateRegistry.createRegistry(8989);
	MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	// 注册MBean,ObjectName为包装过的域名 格式 [父名称 + :name=子名称]
	server.registerMBean(new TestMBeanServiceImpl(), new ObjectName(NAME));

	// 添加会话认证
	Map<String, Object> prop = new HashMap<String, Object>();
	prop.put(JMXConnectorServer.AUTHENTICATOR, new JMXAuthenticator() {
	    public Subject authenticate(Object credentials) {
		if (credentials instanceof String[]) {
		    String[] info = (String[]) credentials;
		    if ("userName".equals(info[0]) && "password".equals(info[1])) {
			return new Subject();
		    }
		}
		throw new SecurityException("not authicated");
	    }
	});

	// 启动JXM Connector Server
	JMXConnectorServer cserver = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(URL), prop, server);
	cserver.start();
	System.out.println("start.....");
    }

}
