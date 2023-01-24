package com.qiuyu.bean;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka消息队列事件（评论、点赞、关注事件
 */
@Getter
public class Event {
    // Kafka必要的主题变量
    private String topic;
    private int userId;
    // 用户发起事件的实体类型（评论、点赞、关注类型）
    private int entityType;
    // 用户发起事件的实体(帖子、评论、用户)id
    private int entityId;
    // 被发起事件的用户id(被评论、被点赞、被关注用户)
    private int entityUserId;
    // 其他可扩充内容对应Comment中的content->显示用户xxx评论、点赞、关注了xxx
    private Map<String,Object> data = new HashMap<>();

    //返回Event方便链式调用
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }
    // 方便外界直接调用key-value,而不用再封装一下传整个Map集合
    public Event setData(String key,Object value) {
        this.data.put(key, value);
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
