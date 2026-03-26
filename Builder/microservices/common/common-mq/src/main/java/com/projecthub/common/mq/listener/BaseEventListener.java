package com.projecthub.common.mq.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.common.mq.domain.EventMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件消费监听器基类
 *
 * @param <T> 事件数据类型
 */
@Slf4j
public abstract class BaseEventListener<T> implements ChannelAwareMessageListener {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理事件消息
     *
     * @param message 事件消息
     * @return true-消费成功，false-消费失败（会重试）
     */
    protected abstract boolean onMessage(EventMessage<T> message);

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        EventMessage<T> eventMessage = null;

        try {
            // 解析事件消息
            eventMessage = parseMessage(message);
            String eventId = eventMessage.getEventId();

            log.info("收到事件：eventId={}, type={}, source={}",
                    eventId, eventMessage.getEventType(), eventMessage.getSource());

            // 调用子类处理方法
            boolean success = onMessage(eventMessage);

            if (success) {
                // 手动确认
                channel.basicAck(deliveryTag, false);
                log.info("事件处理成功：eventId={}", eventId);
            } else {
                // 处理失败，拒绝消息并重新入队
                log.warn("事件处理失败，拒绝消息：eventId={}", eventId);
                channel.basicNack(deliveryTag, false, true);
            }

        } catch (Exception e) {
            String eventId = eventMessage != null ? eventMessage.getEventId() : "unknown";
            log.error("事件处理异常：eventId={}", eventId, e);

            // 异常时拒绝消息，不重新入队（避免无限重试）
            channel.basicNack(deliveryTag, false, false);

            // 可以发送到死信队列
            handleDlxMessage(message, e);
        }
    }

    /**
     * 解析消息
     */
    @SuppressWarnings("unchecked")
    protected EventMessage<T> parseMessage(Message message) throws Exception {
        byte[] body = message.getBody();
        String json = new String(body, "UTF-8");

        // 获取泛型实际类型
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType parameterizedType) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];

            // 先解析为 Map
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            // 创建 EventMessage 实例
            EventMessage<T> eventMessage = new EventMessage<>();
            eventMessage.setEventId(getStringValue(map, "eventId"));
            eventMessage.setEventType(getStringValue(map, "eventType"));
            eventMessage.setSource(getStringValue(map, "source"));
            eventMessage.setVersion(getStringValue(map, "version"));

            // 解析时间戳
            Object timestampObj = map.get("timestamp");
            if (timestampObj != null) {
                // 处理 LocalDateTime 类型
                if (timestampObj instanceof Map tsMap) {
                    eventMessage.setTimestamp(java.time.LocalDateTime.now()); // 简化处理
                }
            }

            // 解析 data 字段
            Object dataObj = map.get("data");
            if (dataObj != null) {
                String dataJson = objectMapper.writeValueAsString(dataObj);
                T data = objectMapper.readValue(dataJson, (Class<T>) getDataType(actualType));
                eventMessage.setData(data);
            }

            return eventMessage;
        }

        throw new IllegalStateException("无法获取泛型类型");
    }

    /**
     * 从 Map 中获取字符串值
     */
    protected String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取 Data 类型
     */
    @SuppressWarnings("unchecked")
    protected Class<?> getDataType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        // 默认返回 Map
        return Map.class;
    }

    /**
     * 处理死信消息
     */
    protected void handleDlxMessage(Message message, Exception e) {
        log.warn("消息将进入死信队列：{}", e.getMessage());
        // 可以在这里记录日志或发送到告警系统
    }

}
