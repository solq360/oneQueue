package com.eyu.onequeue.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class TestBossChildGroup {
    static SocketAddress address = new InetSocketAddress("localhost", 8877);

    @Test
    public void server() throws IOException {

	SelectorProvider bossProvider = SelectorProvider.provider();
	SelectorProvider childProvider = SelectorProvider.provider();

	int count = 2;
	AbstractSelector bossSelector = bossProvider.openSelector();
	AbstractSelector[] childSelectors = new AbstractSelector[count];
	for (int i = 0; i < count; i++) {
	    childSelectors[i] = childProvider.openSelector();
	}

	//server绑定访问端口 并向Selector注册OP_ACCEPT
	ServerSocketChannel serverSocketChannel = bossProvider.openServerSocketChannel();
	serverSocketChannel.configureBlocking(false);
	serverSocketChannel.bind(address);
	serverSocketChannel.register(bossSelector, SelectionKey.OP_ACCEPT);  
	
	int i = 0;
	while (true) {
	    int s = bossSelector.select(300);
	    if (s > 0) {
		Set<SelectionKey> keys = bossSelector.selectedKeys();
		Iterator<SelectionKey> it = keys.iterator();
		while (it.hasNext()) {
		    SelectionKey key = it.next();
		    //为什么不用elseIf 因为 key interestOps 是多重叠状态，一次返回多个操作
		    if (key.isAcceptable()) {
			System.out.println("isAcceptable");
			//这里比较巧妙，注册OP_READ交给别一个Selector处理
			key.channel().register(childSelectors[i++ % count], SelectionKey.OP_READ);
		    }
		    //这部分是child eventLoop处理
		    if (key.isConnectable()) {
			System.out.println("isConnectable");
		    }
		    if (key.isWritable()) {
			System.out.println("isWritable");
		    }
		    if (key.isReadable()) {
			System.out.println("isReadable");
		    }
		    key.interestOps(~key.interestOps());
		    it.remove();
		}
	    }
	}

    }

    @Test
    public void client() throws IOException {
	SocketChannel clientSocketChannel = SelectorProvider.provider().openSocketChannel();
	clientSocketChannel.configureBlocking(true);
	clientSocketChannel.connect(address);
    }
}
