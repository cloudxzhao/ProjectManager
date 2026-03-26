package com.projecthub.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 熔断降级配置
 *
 * 注：Sentinel 熔断降级规则通过 Nacos 配置中心动态管理
 * 配置 Data ID: gateway-service-sentinel.yaml
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    private final SentinelProperties sentinelProperties;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        log.info("熔断降级配置已加载，熔断时长：{}ms",
            sentinelProperties.getCircuitBreaker().getSleepWindowMs());
    }

}
