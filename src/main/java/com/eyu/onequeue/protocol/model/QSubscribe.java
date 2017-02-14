package com.eyu.onequeue.protocol.model;

/**
 * 
 * @author solq
 */
public class QSubscribe {
    /**
     * topic
     */
    private String topic;
    /**
     * groudId
     */
    private String groupId;

    public String getTopic() {
	return topic;
    }

    public String getGroupId() {
	return groupId;
    }

    @Override
    public String toString() {
	return topic + ":" + groupId;
    }

    public static QSubscribe of(String topic, String groupId) {
	QSubscribe ret = new QSubscribe();
	ret.topic = topic;
	ret.groupId = groupId;
	return ret;
    }

}
