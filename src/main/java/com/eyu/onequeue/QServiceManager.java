package com.eyu.onequeue;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eyu.onequeue.push.QPushManager;
import com.eyu.onequeue.socket.service.QNodeManager;
import com.eyu.onequeue.store.service.QMStoreService;

@Component
public class QServiceManager {

    @Autowired
    private QMStoreService storeService;

    @Autowired
    private QPushManager pushManager;

    @Autowired
    private QNodeManager nodeManager;

    @PreDestroy
    private void preDestroy() {
	if (pushManager != null) {
	    pushManager.close();
	}
	if (nodeManager != null) {
	    nodeManager.close();
	}
	storeService.close();
    }
}
