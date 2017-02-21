package com.eyu.onequeue.demo.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public final class ServerBio {
    private static int DEFAULT_PORT = 12345;
    private static ServerSocket server;
    private static AtomicInteger ai = new AtomicInteger();

    public static void main(String[] args) throws Exception {
	try {
	    server = new ServerSocket(DEFAULT_PORT);
	    System.out.println("服务器已启动，端口号：" + DEFAULT_PORT);
	    while (true) {
		Socket socket = server.accept();
		ai.incrementAndGet();
		new Thread(new ServerHandler(socket)).start();
	    }
	} finally {
	    // 一些必要的清理工作
	    if (server != null) {
		System.out.println("服务器已关闭。");
		server.close();
		server = null;
	    }
	}
    }

    public static class ServerHandler implements Runnable {
	private Socket socket;
	private BufferedReader in = null;
	private PrintWriter out = null;

	public ServerHandler(Socket socket) {
	    this.socket = socket;
	}

	@Override
	public void run() {
	    try {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		String body;
		while (true) {
		    if ((body = in.readLine()) == null) {
			 System.out.println("continue");
			continue;
		    }
		    System.out.println("服务器收到消息：" + body);
		    out.println(ai.get());
		}
	    } catch (Exception e) {
		 
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
		if (out != null) {
		    out.close();
		}
		if (socket != null) {
		    try {
			socket.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
    }
}