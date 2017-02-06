package com.eyu.onequeue.socket.handler;

import com.eyu.onequeue.protocol.model.QPacket;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.socket.model.IQResponseHandler;

/***
 * 响应生产端业务处理
 * 
 * @author solq
 */
public class ProduceResponseHandler implements IQResponseHandler<QProduce> {

    // QPacket packet = QPacket.of(ofProduce());
    //
    // QProduce ofProduce() {
    // int count = 100000;
    // TestMessageObject obj = TestMessageObject.of();
    // List<QMessage<Long, TestMessageObject>> tmp = new ArrayList<>();
    // for (int i = 0; i < count; i++) {
    // tmp.add(QMessage.of(1L, obj));
    // }
    // QProduce qm = QProduce.of("ABC", tmp.toArray(new QMessage[count]));
    // return qm;
    // }
    //
    // void doTestTask(ChannelHandlerContext ctx) {
    // System.out.println(PacketUtil.toMSize(packet.toSize()));
    // System.out.println(packet.toSize());
    // TimeUtil.record("send start : ");
    // ctx.writeAndFlush(packet);
    // TimeUtil.println("send end : ");
    // }

    @Override
    public void onReceive(QPacket qpacket, QProduce produce) {
	// TODO Auto-generated method stub

    }

}
