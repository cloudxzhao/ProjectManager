package com.projecthub.common.core.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 自定义健康检查指示器
 *
 * 用于检查服务的健康状态
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查关键组件
        boolean databaseOk = checkDatabase();
        boolean redisOk = checkRedis();
        boolean mqOk = checkRabbitMQ();

        if (databaseOk && redisOk && mqOk) {
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("redis", "UP")
                    .withDetail("rabbitmq", "UP")
                    .build();
        }

        // 构建详细状态
        Health.Builder builder = Health.unknown();
        builder.withDetail("database", databaseOk ? "UP" : "DOWN");
        builder.withDetail("redis", redisOk ? "UP" : "DOWN");
        builder.withDetail("rabbitmq", mqOk ? "UP" : "DOWN");

        return builder.build();
    }

    /**
     * 检查数据库连接
     */
    private boolean checkDatabase() {
        // 实际项目中应该检查数据库连接
        // 可以使用 Spring Boot 自带的 DataSourceHealthIndicator
        return true;
    }

    /**
     * 检查 Redis 连接
     */
    private boolean checkRedis() {
        // 实际项目中应该检查 Redis 连接
        // 可以使用 Spring Boot 自带的 RedisHealthIndicator
        return true;
    }

    /**
     * 检查 RabbitMQ 连接
     */
    private boolean checkRabbitMQ() {
        // 实际项目中应该检查 RabbitMQ 连接
        // 可以使用 Spring Boot 自带的 RabbitHealthIndicator
        return true;
    }

}
