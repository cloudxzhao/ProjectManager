package com.projecthub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Sentinel 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "sentinel")
public class SentinelProperties {

    /**
     * 默认限流阈值（每秒请求数）
     */
    private Integer defaultFlowCount = 100;

    /**
     * 默认统计时间窗口（秒）
     */
    private Integer defaultIntervalSec = 1;

    /**
     * 默认突发流量大小
     */
    private Integer defaultBurst = 200;

    /**
     * 默认控制行为（0-快速失败，1-冷启动，2-排队等待）
     */
    private Integer defaultControlBehavior = 0;

    /**
     * 默认排队超时时间（毫秒）
     */
    private Integer defaultQueueTimeoutMs = 0;

    /**
     * 熔断配置
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    @Data
    public static class CircuitBreaker {
        /**
         * 最小请求数
         */
        private Integer minRequestAmount = 10;

        /**
         * 慢调用比例阈值（0-1 之间）
         */
        private Double slowRatioThreshold = 0.8;

        /**
         * 慢调用 RT 阈值（毫秒）
         */
        private Integer slowRtMs = 5000;

        /**
         * 异常比例阈值（0-1 之间）
         */
        private Double errorRatioThreshold = 0.5;

        /**
         * 异常数阈值
         */
        private Integer errorAmount = 10;

        /**
         * 熔断时长（秒）
         */
        private Integer sleepWindowMs = 30000;

        /**
         * 熔断策略（0-慢调用，1-异常比例，2-异常数）
         */
        private Integer strategy = 0;
    }

}
