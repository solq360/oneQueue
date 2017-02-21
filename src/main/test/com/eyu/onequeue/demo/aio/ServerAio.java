package com.eyu.onequeue.demo.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerAio implements Runnable {

    private final static AtomicInteger ai = new AtomicInteger();
    private int port = 8889;
    private int threadSize = 10;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private AsynchronousServerSocketChannel serverChannel;

    public ServerAio(int port, int threadSize) {
	this.port = port;
	this.threadSize = threadSize;
    }

    public static void main(String[] args) throws IOException {
	new ServerAio(8989, Runtime.getRuntime().availableProcessors() * 10).run();
	System.in.read();
    }

    public void run() {
	try {
	    asynchronousChannelGroup = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), threadSize);
	    serverChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
	    serverChannel.bind(new InetSocketAddress(port));
	    System.out.println("listening on port: " + port);
	    serverChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, ServerAio>() {

		public void completed(AsynchronousSocketChannel result, ServerAio attachment) {
		    try {
			System.out.println(ai.getAndIncrement());
			ByteBuffer echoBuffer = ByteBuffer.allocateDirect(512);
			result.read(echoBuffer, null, new CompletionHandler<Integer, Object>() {
			    @Override
			    public void completed(Integer result, Object attachment) {
				// System.out.println("received : " +
				// Charset.defaultCharset().decode(echoBuffer));
			    }

			    @Override
			    public void failed(Throwable exc, Object attachment) {
			    }
			});

			result.write(ByteBuffer.wrap("ok".getBytes()));
		    } catch (Exception e) {
			e.printStackTrace();
		    } finally {
			attachment.serverChannel.accept(attachment, this);// 监听新的请求，递归调用。
		    }
		}

		public void failed(Throwable exc, ServerAio attachment) {
		    System.out.println("received failed");
		    exc.printStackTrace();
		    attachment.serverChannel.accept(attachment, this);// 监听新的请求，递归调用。
		}
	    });

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}