package com.projecthub.notification.consumer;

import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.notification.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    /**
     * 消费通知队列消息
     */
    @RabbitListener(queuesToDeclare = @Queue(
            value = "projecthub.notification",
            durable = "true",
            autoDelete = "false"
    ))
    public void handleNotification(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到通知消息：messageId={}", message.getMessageProperties().getMessageId());

            // 解析消息
            EventMessage<?> eventMessage = (EventMessage<?>) message.getBody();

            // 处理通知
            notificationService.processEvent(eventMessage);

            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理通知消息失败：deliveryTag={}", deliveryTag, e);
            try {
                // 拒绝消息，不重新入队（避免死循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("拒绝消息失败：deliveryTag={}", deliveryTag, ex);
            }
        }
    }
}
