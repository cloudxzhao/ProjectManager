package com.projecthub.common.mq.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 事件消息基类
 */
@Data
public class EventMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件 ID（用于幂等性）
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件来源服务
     */
    private String source;

    /**
     * 事件时间
     */
    private LocalDateTime timestamp;

    /**
     * 事件数据
     */
    private T data;

    /**
     * 版本号
     */
    private String version = "1.0";

    public EventMessage() {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.timestamp = LocalDateTime.now();
    }

    public EventMessage(String eventType, String source, T data) {
        this();
        this.eventType = eventType;
        this.source = source;
        this.data = data;
    }

    /**
     * 创建事件消息
     */
    public static <T> EventMessage<T> of(String eventType, String source, T data) {
        return new EventMessage<>(eventType, source, data);
    }

}