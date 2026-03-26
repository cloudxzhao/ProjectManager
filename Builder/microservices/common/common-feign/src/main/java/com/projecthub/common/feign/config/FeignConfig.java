package com.projecthub.common.feign.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 全局配置
 */
@Configuration
public class FeignConfig {

    /**
     * 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 请求超时配置
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true);
    }

    /**
     * 重试配置（不重试）
     */
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

}

/**
 * Sentinel 熔断降级配置说明
 *
 * 在 application.yaml 中配置:
 *
 * spring:
 *   cloud:
 *     sentinel:
 *       enabled: true
 *       transport:
 *         dashboard: localhost:8080  # Sentinel Dashboard 地址
 *       datasource:
 *         ds1:
 *           nacos:
 *             server-addr: localhost:8848
 *             dataId: ${spring.application.name}-sentinel.json
 *             groupId: DEFAULT_GROUP
 *             rule-type: flow
 *
 * 熔断降级规则示例 (JSON):
 * [
 *   {
 *     "resource": "GET#GET#http://user-service/api/v1/users/{id}",
 *     "count": 5.0,
 *     "timeWindow": 10,
 *     "grade": 0,
 *     "limitMode": 0,
 *     "limitApp": "default"
 *   }
 * ]
 *
 * grade: 0=响应时间，1=异常比例，2=异常数量
 * timeWindow: 熔断时长（秒）
 * count: 阈值
 */