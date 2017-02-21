package com.eyu.onequeue.demo.stcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSCTP {
    static int SERVER_PORT = 8867;
    static int US_STREAM = 0;
    static int FR_STREAM = 1;
    private final static AtomicInteger ai = new AtomicInteger();

    static SimpleDateFormat USformatter = new SimpleDateFormat("h:mm:ss a EEE d MMM yy, zzzz", Locale.US);
    static SimpleDateFormat FRformatter = new SimpleDateFormat("h:mm:ss a EEE d MMM yy, zzzz", Locale.FRENCH);

    @SuppressWarnings("restriction")
    public static void main(String[] args) throws IOException {
	SERVER_PORT = args.length > 0 ? Integer.valueOf(args[0]) : SERVER_PORT;
	com.sun.nio.sctp.SctpServerChannel ssc = com.sun.nio.sctp.SctpServerChannel.open();
	ssc.bind(new InetSocketAddress(SERVER_PORT));

	ByteBuffer buf = ByteBuffer.allocateDirect(60);
	CharBuffer cbuf = CharBuffer.allocate(60);
	CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
	try {
	    while (true) {
		com.sun.nio.sctp.SctpChannel sc = ssc.accept();
		buf.clear();
		buf.putInt(ai.getAndIncrement());
		buf.flip();
		com.sun.nio.sctp.MessageInfo messageInfo = com.sun.nio.sctp.MessageInfo.createOutgoing(null, US_STREAM);

		sc.send(buf, messageInfo);
		System.out.println(ai.get());
		/* get the current date */
		// Date today = new Date();
		// cbuf.put(USformatter.format(today)).flip();
		// encoder.encode(cbuf, buf, true);
		// buf.flip();
		//
		// /* send the message on the US stream */
		// com.sun.nio.sctp.MessageInfo messageInfo =
		// com.sun.nio.sctp.MessageInfo.createOutgoing(null, US_STREAM);
		// sc.send(buf, messageInfo);
		//
		// /* update the buffer with French format */
		// cbuf.clear();
		// cbuf.put(FRformatter.format(today)).flip();
		// buf.clear();
		// encoder.encode(cbuf, buf, true);
		// buf.flip();
		//
		// /* send the message on the French stream */
		// messageInfo.streamNumber(FR_STREAM);
		// sc.send(buf, messageInfo);
		//
		// cbuf.clear();
		// buf.clear();
		//
		// sc.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	ssc.close();
    }
}