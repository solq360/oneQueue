package com.eyu.onequeue.store.service;

import com.eyu.onequeue.protocol.model.QConsume;
import com.eyu.onequeue.protocol.model.QProduce;
import com.eyu.onequeue.store.model.IQStore;
import com.eyu.onequeue.store.model.QQuery;

/**
 * File实现QMStore 一個 topic 对应一个 Store
 * 
 * @author solq
 */
public class FileQMStore implements IQStore {

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
    public QConsume query(QQuery query) {
	return fileIndexer.query(query);
    }
    @Override
    public QProduce queryForProduce(QQuery query) {
	return fileIndexer.queryForProduce(query);
    }


    @Override
    public void persist() {
	fileIndexer.persist();
    }

    @Override
    public void close() {
	fileIndexer.close();
    }

    @Override
    public void start() {
	
    }

 
}
