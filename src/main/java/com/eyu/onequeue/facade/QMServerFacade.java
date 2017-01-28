package com.eyu.onequeue.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eyu.onequeue.push.PushMonitor;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.store.QMStoreService;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;

/**
 * 对外公开服务
 * @author solq
 * */
@Service
public class QMServerFacade implements IQMServerFacade {

	@Autowired
	private QMStoreService storeService;
	@Autowired
	private PushMonitor pushMonitor;

	@Override
	public void save(String topic, Object... messages) {
		storeService.save(topic, messages);
	}

	@Override
	public QResult query(QQuery query) {
		return storeService.query(query);
	}

	@Override
	public void persist() {
		storeService.persist();
		//pushMonitor.persist();
	}

	@Override
	public void close() {
		storeService.close();
		pushMonitor.shutdown();
	}

	@Override
	public void registerNode(String topic, String groupId, QNode node) {
		pushMonitor.registerNode(topic, groupId, node);
	}

	@Override
	public void removeNode(String topic, String groupId, QNode node) {
		pushMonitor.registerNode(topic, groupId, node);
	}

	@Override
	public void removeNode(QNode node) {
		pushMonitor.removeNode(node);
	}

}
