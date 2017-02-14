package com.eyu.onequeue.push;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.model.IQService;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.store.model.IQStoreService;
import com.eyu.onequeue.util.PoolUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author solq
 **/
public class QPushManager implements IQService {
    private final static ExecutorService pool = PoolUtil.createPool(QMConfig.getInstance().POOL_PUSH_CORE, 60, "PushMonitor");
    private volatile boolean initializer = false;
    private volatile boolean closed = false;
    
    private IQStoreService storeService;

    private Map<String, Subscribe> subs = new HashMap<>();
    private long lastPersistRecord = System.currentTimeMillis();
    private Thread processPushThread = new Thread(new Runnable() {

	@Override
	public void run() {
	    while (!closed) {
		try {
		    Thread.sleep(QMConfig.getInstance().PUSH_MESSAGE_INTERVAL);
		} catch (InterruptedException e) {

		}
		if (closed) {
		    break;
		}
		Map<String, Subscribe> subs = getSubs();
		for (Entry<String, Subscribe> entry : subs.entrySet()) {
		    pool.submit(() -> {
			if (closed) {
			    return;
			}
			Subscribe sub = entry.getValue();
			sub.push(storeService);
		    });
		}

		if ((System.currentTimeMillis() - lastPersistRecord) > QMConfig.getInstance().PUSH_PERSIST_INTERVAL) {
		    lastPersistRecord = System.currentTimeMillis();
		    persist();
		}

	    }
	}
    }, "processPushThread");

    public QPushManager(IQStoreService storeService) {
	this.storeService = storeService;
    }

    public void registerNode(String topic, String groupId, QNode node) {
	Subscribe sub = getSub(topic, groupId);
	sub.addNode(node);
    }

    public void removeNode(String topic, String groupId, QNode node) {
	Subscribe sub = getSub(topic, groupId);
	sub.removeNode(node);
    }

    public void removeNode(QNode node) {
	Map<String, Subscribe> subs = getSubs();
	for (Subscribe sub : subs.values()) {
	    sub.removeNode(node);
	}
    }

    @Override
    public void start() {
	if(initializer){
	    return;
	}
	initializer = true;
	closed =false;
	processPushThread.setDaemon(true);
	processPushThread.start();
 	String thisFileName = QMConfig.getInstance().getPushPersistPath();
	if (new File(thisFileName).exists()) {
	    subs = SerialUtil.readValueAsFile(thisFileName, new TypeReference<HashMap<String, Subscribe>>() {
	    });
	}
    }

    public void close() {
	if (closed) {
	    return;
	}
	initializer = false;
	closed = true;
	PoolUtil.shutdown(pool, 60);
	persist();
    }

    //////////////////////////////////////////////////
    private synchronized void persist() {
	String thisFileName = QMConfig.getInstance().getPushPersistPath();;
	SerialUtil.writeValueAsFile(thisFileName, subs);
    }

    private Subscribe getSub(String topic, String groupId) {
	final String key = topic + ":" + groupId;
	Subscribe ret = subs.get(key);
	if (ret != null) {
	    return ret;
	}
	synchronized (this) {
	    ret = subs.get(key);
	    if (ret != null) {
		return ret;
	    }
	    ret = Subscribe.of(topic, groupId);
	    subs.put(key, ret);
	}
	return ret;
    }

    private synchronized Map<String, Subscribe> getSubs() {
	return new HashMap<>(this.subs);
    }

}
