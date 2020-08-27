package com.yuan.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件的实体类，用于发送系统通知
 */
public class Event {

    private String topic; //主题，事件的类型
    private int userId;  // 事件触发人
    private int entityType;  // 事件类型，评论、点赞、关注
    private int entityId;
    private int entityUserId;  // 事件的接受通知人
    private Map<String, Object> data = new HashMap<>();  //用于扩展其他事件使用

    public String getTopic() {
        return topic;
    }

    // 为了方便构造对象时更方便，在setTopic方法加返回值
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

}
