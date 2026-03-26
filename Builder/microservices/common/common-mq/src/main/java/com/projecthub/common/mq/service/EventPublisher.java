package com.projecthub.common.mq.service;

import com.projecthub.common.mq.domain.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 事件发布服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送事件到指定交换机
     */
    public <T> void publish(String exchange, String routingKey, EventMessage<T> message) {
        log.info("发送事件: exchange={}, routingKey={}, eventId={}, eventType={}",
                exchange, routingKey, message.getEventId(), message.getEventType());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送事件到默认 Topic 交换机
     */
    public <T> void publish(String routingKey, EventMessage<T> message) {
        publish("projecthub.topic", routingKey, message);
    }

    /**
     * 发送延迟消息
     */
    public <T> void publishDelayed(String routingKey, EventMessage<T> message, long delayMs) {
        log.info("发送延迟事件: routingKey={}, eventId={}, delayMs={}",
                routingKey, message.getEventId(), delayMs);
        rabbitTemplate.convertAndSend("projecthub.delayed", routingKey, message, msg -> {
            msg.getMessageProperties().setDelayLong(delayMs);
            return msg;
        });
    }

}