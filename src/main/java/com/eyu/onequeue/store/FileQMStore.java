package com.eyu.onequeue.store;

import com.eyu.onequeue.store.model.IQMStore;
import com.eyu.onequeue.store.model.QQuery;
import com.eyu.onequeue.store.model.QResult;

/**
 * File实现QMStore 一個 topic 对于一个 Store
 * 
 * @author solq
 */
public class FileQMStore implements IQMStore {

    @SuppressWarnings("unused")
    private String topic;

    private FileIndexer fileIndexer;

    public static FileQMStore of(String topic, FileIndexer fileIndexer) {
	FileQMStore ret = new FileQMStore();
	ret.topic = topic;
	ret.fileIndexer = fileIndexer;
	return ret;
    }

    @Override
    public void save(Object... messages) {
	fileIndexer.write(messages);
    }

    @Override
    public void save(byte[] bytes) {
	fileIndexer.write(bytes);
    }

    @Override
    public QResult query(QQuery query) {
	return fileIndexer.query(query);
    }

//    @Override
//    public QResult queryForRaw(QQuery query) {
//	return fileIndexer.queryForRaw(query);
//    }

    @Override
    public void persist() {
	fileIndexer.persist();
    }

    @Override
    public void close() {
	fileIndexer.close();
    }

}
