package com.eyu.onequeue.socket.model;

import java.util.HashMap;
import java.util.Map;

import com.eyu.onequeue.protocol.model.IRecycle;

/***
 * @author solq
 */
@SuppressWarnings("unchecked")
public class QSession implements IRecycle {

    public static enum SESSION_KEY {
	ALIAS, OP_TIME, ID,
    }

    private long id;
    private Map<String, Object> values = new HashMap<>();

    public static QSession of(String id) {
	long v = 0;
	try {
	    v = Long.valueOf(id);
	} catch (Exception e) {
	    v = id.hashCode();
	}
	QSession ret = new QSession();
	ret.id = v;
	ret.put(SESSION_KEY.ID, id);
	return ret;
    }

    public static QSession of(long id) {
	QSession ret = new QSession();
	ret.id = id;
	ret.put(SESSION_KEY.ID, id);
	return ret;
    }

    ///////////////////////////////////////////
    public synchronized void put(String key, Object value) {
	values.put(key, value);
    }

    public void put(SESSION_KEY key, Object value) {
	put(key.name(), value);
    }

    public synchronized <T> T remove(String key) {
	return (T) values.remove(key);
    }

    public synchronized <T> T get(String key) {
	return (T) values.get(key);
    }

    public synchronized void replace(QSession oldSession) {
	Map<String, Object> old = oldSession.getValues();
	old.putAll(values);
	values = old;
    }

    @Override
    public synchronized void recycle() {
	values.clear();
    }

    public synchronized Map<String, Object> getValues() {
	return new HashMap<>(values);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (int) (id ^ (id >>> 32));
	return result;
    }

    // getter

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	QSession other = (QSession) obj;
	if (id != other.id)
	    return false;
	return true;
    }

    public long getId() {
	return id;
    }

}
