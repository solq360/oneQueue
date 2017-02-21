package com.eyu.onequeue.store.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyu.onequeue.QMConfig;
import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.store.model.IQStore;
import com.eyu.onequeue.store.model.IQStoreService;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.util.NumRecordUtil;
import com.eyu.onequeue.util.PoolUtil;

/***
 * 提供对其它模块服务
 * 
 * @author solq
 */

public class QStoreService implements IQStoreService {
    private final static Logger logger = LoggerFactory.getLogger(QStoreService.class);
    private Map<String, IQStore> stores = new HashMap<>();
    private int useStoreCount = 0;
    private final static ExecutorService pool = PoolUtil.createPool(QMConfig.getInstance().POOL_STORE_CORE, 60 * 2, "fileStore");
    private volatile boolean closed = false;
    private volatile boolean initializer = false;

    private Thread monitor = new Thread(new Runnable() {

	@Override
	public void run() {
	    while (!closed) {
		try {
		    Thread.sleep(QMConfig.getInstance().STORE_FILE_PERSIST_INTERVAL);
		} catch (InterruptedException e) {
		}
		if (closed) {
		    break;
		}
		persist();
	    }
	}
    }, "qm message persist monitor");

    @Override
    public void start() {
	if (initializer) {
	    return;
	}
	initializer = true;
	closed = false;
	monitor.setDaemon(true);
	monitor.start();
    }

    @Override
    public void save(String topic, Object... messages) {
	IQStore store = getStore(topic);
	store.save(messages);
    }

    @Override
    public QConsume query(QQuery query) {
	IQStore store = getStore(query.getTopic());
	return store.query(query);
    }

    @Override
    public void persist() {
	Map<String, IQStore> stores = getStores();
	for (IQStore store : stores.values()) {
	    execute(() -> store.persist());
	}
    }

    @Override
    public void close() {
	if (closed) {
	    return;
	}
	closed = true;
	initializer = false;
	Map<String, IQStore> stores = getStores();
	for (IQStore store : stores.values()) {
	    execute(() -> store.close());
	}
	PoolUtil.shutdown(pool, 60 * 2);
    }

    @Override
    public int getUseStoreCount() {
	return useStoreCount;
    }

    /////////////////////////////////////////////////////////

    private void execute(Runnable task) {
	try {
	    pool.submit(task);
	} catch (Exception e) {
	    logger.error("PushMonitor", e);
	}
    }

    private synchronized Map<String, IQStore> getStores() {
	return new HashMap<>(this.stores);
    }

    private IQStore getStore(String topic) {
	IQStore ret = stores.get(topic);
	if (ret != null) {
	    return ret;
	}
	synchronized (this) {
	    ret = stores.get(topic);
	    if (ret != null) {
		return ret;
	    }
	    ret = FileQMStore.of(topic, FileIndexer.of(topic));
	    stores.put(topic, ret);
	    useStoreCount++;
	}
	return ret;
    }

    @Override
    public long getUseMemoryCount() {
	return NumRecordUtil.STORE_MEMORY.getValue();
    }

}
