package com.eyu.onequeue.socket.handle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.eyu.onequeue.protocol.model.QMessage;
import com.eyu.onequeue.socket.model.IConsumeHandle;
import com.eyu.onequeue.util.PacketUtil;
import com.eyu.onequeue.util.SerialUtil;

/***
 * 消费端业务逻辑
 * 
 * @author solq
 */
public class ConsumeHandle implements IConsumeHandle {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onSucceed(String topic, byte[] bytes) {
		List<QMessage<?, ?>> ret = new LinkedList<>();
		byte[] tBytes = null;
		int offset = 0;
		while (true) {
			if (offset >= bytes.length) {
				break;
			}
			int len = 0;
			try {
				len = PacketUtil.readInt(offset, bytes);
				tBytes = SerialUtil.unZip(PacketUtil.readBytes(offset + Integer.BYTES, len, bytes));
				List<QMessage> list = SerialUtil.readArray(tBytes, QMessage.class);
				ret.addAll((Collection<? extends QMessage<?, ?>>) list);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (len == 0) {
				break;
			}
			offset += len;
		}
		doSucceed(topic, ret);
	}

	public void doSucceed(String topic, List<QMessage<?, ?>> qMessages) {

	}
}
