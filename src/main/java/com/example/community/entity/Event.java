package com.example.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装事件，用于消息队列
 * @author Lenovo
 */
public class Event {

    // 主题
    private String topic;
    // 事件发起人id
    private int userId;
    private int entityType;
    private int entityId;
    // 实体（帖子或评论）作者id
    private int entityUserId;
    // 其余的数据或者后序拓展出的数据封装到Map中
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    /**
     * 使set方法返回一个Event，方便继续用.xxx().xxx()直接调用方法连续设置变量
     * @param topic
     * @return
     */
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

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", entityUserId=" + entityUserId +
                ", data=" + data +
                '}';
    }
}
