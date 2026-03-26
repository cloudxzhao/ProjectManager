package com.projecthub.common.mq.consumer;

import com.projecthub.common.mq.domain.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 事件消费监听器基类
 * 提供通用的事件处理逻辑
 *
 * @param <T> 事件数据类型
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventConsumer<T> {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    /**
     * 处理事件的具体逻辑，由子类实现
     *
     * @param eventMessage 事件消息
     */
    protected abstract void handleEvent(EventMessage<T> eventMessage);

    /**
     * 获取事件类型的 Class
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getEventDataClass() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] typeParams = ((ParameterizedType) superClass).getActualTypeArguments();
            if (typeParams.length > 0) {
                return (Class<T>) typeParams[0];
            }
        }
        return null;
    }

    /**
     * RabbitMQ 监听器 - 接收消息并处理
     * 子类可以通过 @RabbitListener 注解自定义队列
     *
     * @param message RabbitMQ 消息
     */
    public void onMessage(Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        try {
            log.info("收到消息: messageId={}, deliveryTag={}", messageId, deliveryTag);

            // 转换消息
            EventMessage<T> eventMessage = (EventMessage<T>) messageConverter.fromMessage(message);

            if (eventMessage == null) {
                log.warn("消息转换失败: messageId={}", messageId);
                rabbitTemplate.getConnectionFactory().createConnection()
                        .createChannel(false).basicNack(deliveryTag, false, false);
                return;
            }

            log.info("处理事件: eventId={}, eventType={}, source={}",
                    eventMessage.getEventId(), eventMessage.getEventType(), eventMessage.getSource());

            // 调用子类处理逻辑
            handleEvent(eventMessage);

            log.info("事件处理成功: eventId={}", eventMessage.getEventId());

        } catch (Exception e) {
            log.error("事件处理失败: messageId={}, deliveryTag={}", messageId, deliveryTag, e);
            throw new RuntimeException("事件处理失败", e);
        }
    }

    /**
     * 发送失败消息到死信队列
     *
     * @param message    原消息
     * @param reason     失败原因
     */
    protected void sendToDeadLetterQueue(Message message, String reason) {
        log.warn("发送消息到死信队列: reason={}", reason);
        // 可以实现重试机制或发送到专门的死信队列
        rabbitTemplate.send("projecthub.dlx", message);
    }

}