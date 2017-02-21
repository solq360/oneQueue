package com.eyu.onequeue.demo.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientAio implements Runnable {
    private final static int count = 50000;
    private final static AsynchronousSocketChannel[] clients = new AsynchronousSocketChannel[count];
    private final static AtomicInteger ai = new AtomicInteger();

    private String host;
    private int port;
    private AsynchronousSocketChannel client;

    public ClientAio(String host, int port) throws IOException {
	this.client = AsynchronousSocketChannel.open();
	this.host = host;
	this.port = port;
    }

    public static void main(String[] args) throws Exception {
	String addr = args.length > 0 ? args[0] : "192.168.56.122";
	while (ai.get() < count) {
	    new ClientAio(addr, 8989).run();
	}
	System.in.read();
    }

    public void run() {
	client.connect(new InetSocketAddress(host, port), null, new CompletionHandler<Void, Object>() {
	    public void completed(Void result, Object attachment) {
		int i = ai.getAndIncrement();
		if (i < count) {
		    clients[i] = client;
		}
 		final ByteBuffer byteBuffer = ByteBuffer.allocate(512);
		client.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
		    public void completed(Integer result, Object attachment) {

			//System.out.println("client read data: " + new String(byteBuffer.array()));
		    }

		    public void failed(Throwable exc, Object attachment) {
			System.out.println("read faield");
		    }
		});
	    }

	    public void failed(Throwable exc, Object attachment) {
		System.out.println("client connect field...");
		try {
		    if(client.isOpen()){
			 client.close();
		    }
		} catch (IOException e) {
 		}
	    }
	});
    }
}