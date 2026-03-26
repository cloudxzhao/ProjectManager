package com.projecthub.common.mq.config;

import com.projecthub.common.mq.constant.MqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 交换机和队列初始化配置
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqExchangeConfig {

    /**
     * RabbitAdmin Bean，用于自动声明交换机和队列
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    // ========== 交换机 ==========

    /**
     * Topic 交换机 - 用于事件发布
     */
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_TOPIC)
                .durable(true)
                .build();
    }

    /**
     * Fanout 交换机 - 用于广播通知
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder.fanoutExchange(MqConstant.EXCHANGE_FANOUT)
                .durable(true)
                .build();
    }

    /**
     * Direct 交换机 - 用于点对点消息
     */
    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(MqConstant.EXCHANGE_DIRECT)
                .durable(true)
                .build();
    }

    /**
     * 延迟交换机 - 用于延迟消息
     */
    @Bean
    public CustomExchange delayedExchange() {
        return ExchangeBuilder.customExchange(MqConstant.EXCHANGE_DELAYED)
                .durable(true)
                .autoDelete(false)
                .type("x-delayed-message")
                .build();
    }

    // ========== 队列 ==========

    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_NOTIFICATION)
                .build();
    }

    /**
     * 邮件队列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_EMAIL)
                .build();
    }

    /**
     * 用户同步队列
     */
    @Bean
    public Queue userSyncQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_USER_SYNC)
                .build();
    }

    // ========== 绑定 ==========

    /**
     * 通知队列绑定到 Topic 交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(topicExchange())
                .with(MqConstant.ROUTING_KEY_NOTIFICATION);
    }

    /**
     * 邮件队列绑定到 Topic 交换机
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(topicExchange())
                .with("email.#");
    }

    /**
     * 用户同步队列绑定到 Topic 交换机
     */
    @Bean
    public Binding userSyncBinding() {
        return BindingBuilder.bind(userSyncQueue())
                .to(topicExchange())
                .with(MqConstant.ROUTING_KEY_USER);
    }

}