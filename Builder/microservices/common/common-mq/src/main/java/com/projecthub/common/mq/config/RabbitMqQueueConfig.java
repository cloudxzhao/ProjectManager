package com.projecthub.common.mq.config;

import com.projecthub.common.mq.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 交换机和队列配置
 */
@Configuration
public class RabbitMqQueueConfig {

    // ========== Topic 交换机 ==========

    /**
     * Topic 交换机 - 用于事件广播
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MqConstant.EXCHANGE_TOPIC, true, false);
    }

    // ========== Direct 交换机 ==========

    /**
     * Direct 交换机 - 用于点对点消息
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(MqConstant.EXCHANGE_DIRECT, true, false);
    }

    // ========== Fanout 交换机 ==========

    /**
     * Fanout 交换机 - 用于广播消息
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(MqConstant.EXCHANGE_FANOUT, true, false);
    }

    // ========== Delayed 交换机（延迟队列）==========

    /**
     * Delayed 交换机 - 用于延迟消息
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MqConstant.EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    // ========== 队列 - 通知相关 ==========

    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_NOTIFICATION)
                .withArgument("x-message-ttl", 60000) // 60 秒 TTL
                .build();
    }

    /**
     * 邮件队列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_EMAIL)
                .withArgument("x-message-ttl", 300000) // 5 分钟 TTL
                .build();
    }

    // ========== 队列 - 用户相关 ==========

    /**
     * 用户同步队列
     */
    @Bean
    public Queue userSyncQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_USER_SYNC).build();
    }

    // ========== 队列 - 任务相关 ==========

    /**
     * 任务分配队列
     */
    @Bean
    public Queue taskAssignQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_TASK_ASSIGN).build();
    }

    // ========== 队列 - 项目相关 ==========

    /**
     * 项目事件队列
     */
    @Bean
    public Queue projectEventQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_PROJECT_EVENT).build();
    }

    // ========== 队列 - Issue 相关 ==========

    /**
     * Issue 事件队列
     */
    @Bean
    public Queue issueEventQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_ISSUE_EVENT).build();
    }

    // ========== 死信队列 ==========

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(MqConstant.DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(MqConstant.DLX_QUEUE).build();
    }

    // ========== 绑定关系 ==========

    /**
     * 绑定用户同步队列到 Topic 交换机
     */
    @Bean
    public Binding userSyncBinding(TopicExchange topicExchange, Queue userSyncQueue) {
        return BindingBuilder.bind(userSyncQueue)
                .to(topicExchange)
                .with(MqConstant.ROUTING_KEY_USER);
    }

    /**
     * 绑定通知队列到 Fanout 交换机
     */
    @Bean
    public Binding notificationBinding(FanoutExchange fanoutExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue)
                .to(fanoutExchange);
    }

    /**
     * 绑定邮件队列到 Direct 交换机
     */
    @Bean
    public Binding emailBinding(DirectExchange directExchange, Queue emailQueue) {
        return BindingBuilder.bind(emailQueue)
                .to(directExchange)
                .with("email");
    }

    /**
     * 绑定任务分配队列到 Topic 交换机
     */
    @Bean
    public Binding taskAssignBinding(TopicExchange topicExchange, Queue taskAssignQueue) {
        return BindingBuilder.bind(taskAssignQueue)
                .to(topicExchange)
                .with(MqConstant.ROUTING_KEY_TASK + ".assign");
    }

    /**
     * 绑定项目事件队列到 Topic 交换机
     */
    @Bean
    public Binding projectEventBinding(TopicExchange topicExchange, Queue projectEventQueue) {
        return BindingBuilder.bind(projectEventQueue)
                .to(topicExchange)
                .with(MqConstant.ROUTING_KEY_PROJECT);
    }

    /**
     * 绑定 Issue 事件队列到 Topic 交换机
     */
    @Bean
    public Binding issueEventBinding(TopicExchange topicExchange, Queue issueEventQueue) {
        return BindingBuilder.bind(issueEventQueue)
                .to(topicExchange)
                .with(MqConstant.ROUTING_KEY_TASK + ".issue");
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding dlqBinding(DirectExchange dlxExchange, Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue)
                .to(dlxExchange)
                .with("dlx");
    }

}
