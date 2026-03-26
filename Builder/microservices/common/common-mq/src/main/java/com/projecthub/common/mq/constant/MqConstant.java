package com.projecthub.common.mq.constant;

/**
 * 交换机和队列常量
 */
public interface MqConstant {

    // ========== 交换机 ==========
    String EXCHANGE_DIRECT = "projecthub.direct";
    String EXCHANGE_TOPIC = "projecthub.topic";
    String EXCHANGE_FANOUT = "projecthub.fanout";
    String EXCHANGE_DELAYED = "projecthub.delayed";

    // ========== 队列 ==========
    String QUEUE_NOTIFICATION = "projecthub.notification";
    String QUEUE_EMAIL = "projecthub.email";
    String QUEUE_USER_SYNC = "projecthub.user.sync";
    String QUEUE_TASK_ASSIGN = "projecthub.task.assign";
    String QUEUE_PROJECT_EVENT = "projecthub.project.event";
    String QUEUE_ISSUE_EVENT = "projecthub.issue.event";

    // ========== 路由键 ==========
    String ROUTING_KEY_USER = "user.#";
    String ROUTING_KEY_PROJECT = "project.#";
    String ROUTING_KEY_TASK = "task.#";
    String ROUTING_KEY_NOTIFICATION = "notification.#";

    // ========== 死信队列 ==========
    String DLX_EXCHANGE = "projecthub.dlx";
    String DLX_QUEUE = "projecthub.dlq";
}