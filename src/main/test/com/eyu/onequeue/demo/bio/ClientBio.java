package com.eyu.onequeue.demo.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientBio {
    private static int DEFAULT_SERVER_PORT = 12345;
    private static String DEFAULT_SERVER_IP = "127.0.0.1";
    private static AtomicInteger ai = new AtomicInteger();

    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;

    public static void main(String[] args) throws InterruptedException {
	while (true) {
	    send("xxxxxx");
	    Thread.sleep(1);
	}
    }

    public static void send(String body) {
	new ClientBio().send(DEFAULT_SERVER_PORT, body);
    }

    public void send(int port, String body) {
	try {
	    socket = new Socket(DEFAULT_SERVER_IP, port);
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    out = new PrintWriter(socket.getOutputStream(), true);
	    out.println(body);
	    ai.incrementAndGet();
	    System.out.println("客户端 接收：" + in.readLine());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
