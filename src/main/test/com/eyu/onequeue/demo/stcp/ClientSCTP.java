package com.eyu.onequeue.demo.stcp;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

@SuppressWarnings("restriction")
public class ClientSCTP {
    static int SERVER_PORT = 8867;
    static Charset charset = Charset.forName("ISO-8859-1");
    static CharsetDecoder decoder = charset.newDecoder();

    public void run() throws Exception {

	ByteBuffer buf = ByteBuffer.allocateDirect(60);
	com.sun.nio.sctp.SctpChannel sc = com.sun.nio.sctp.SctpChannel.open();
	sc.connect(new InetSocketAddress("127.0.0.1", SERVER_PORT));
 
	AssociationHandler assocHandler = new AssociationHandler();
	com.sun.nio.sctp.MessageInfo messageInfo = null;
	messageInfo = sc.receive(buf, System.out, assocHandler);
	// if (buf.remaining() > 0 && messageInfo.streamNumber() ==
	// ServerSTCP.US_STREAM) {
	// System.out.println("(US) " + decoder.decode(buf).toString());
	// } else if (buf.remaining() > 0 && messageInfo.streamNumber() ==
	// ServerSTCP.FR_STREAM) {
	// System.out.println("(FR) " + decoder.decode(buf).toString());
	// }

    }

    public static void main(String[] args) throws Exception {
	SERVER_PORT = args.length > 0 ? Integer.valueOf(args[0]) : SERVER_PORT;
	int count = 60000;
	while (count-- > 0) {
	    new ClientSCTP().run();
	}
    }

    static class AssociationHandler extends com.sun.nio.sctp.AbstractNotificationHandler<PrintStream> {
	public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.AssociationChangeNotification not, PrintStream stream) {
	    if (not.event().equals(com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP)) {
		int outbound = not.association().maxOutboundStreams();
		int inbound = not.association().maxInboundStreams();
		stream.printf("New association setup with %d outbound streams" + ", and %d inbound streams.\n", outbound, inbound);
	    }
   	    return com.sun.nio.sctp.HandlerResult.CONTINUE;
	}

	public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.ShutdownNotification not, PrintStream stream) {
	    stream.printf("The association has been shutdown.\n");
	    return com.sun.nio.sctp.HandlerResult.RETURN;
	}
    }
}