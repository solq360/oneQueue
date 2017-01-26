package com.eyu.onequeue.store;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.eyu.onequeue.store.model.IQMStore;
import com.eyu.onequeue.store.model.IQMStoreService;
import com.eyu.onequeue.store.model.IStoreMBean;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;

/***
 * 提供对其它模块服务
 * 
 * @author solq
 */
@Service
public class QMStoreService implements IQMStoreService, IStoreMBean {

    private Map<String, IQMStore> stores = new HashMap<>();
    private int storeCount = 0;

    @Override
    public void save(String topic, Object... messages) {
	IQMStore store = getStore(topic);
	store.save(messages);
    }

    @Override
    public QResult query(QQuery query) {
	IQMStore store = getStore(query.getTopic());
	return store.query(query);
    }

    @Override
    public void persist() {
	Map<String, IQMStore> stores = getStores();
	for (IQMStore store : stores.values()) {
	    store.persist();
	}
    }

    @Override
    public void close() {
	Map<String, IQMStore> stores = getStores();
	for (IQMStore store : stores.values()) {
	    store.close();
	}
    }

    @Override
    public int getStoreCount() {
	return storeCount;
    }

    /////////////////////////////////////////////////////////
    private synchronized Map<String, IQMStore> getStores() {
	return new HashMap<>(this.stores);
    }

    private IQMStore getStore(String topic) {
	IQMStore ret = stores.get(topic);
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
	    storeCount++;
	}
	return ret;
    }
}
