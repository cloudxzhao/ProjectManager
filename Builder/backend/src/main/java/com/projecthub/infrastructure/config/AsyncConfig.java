package com.projecthub.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** 异步配置 */
@Configuration
@EnableAsync
public class AsyncConfig {
  // 通过 @EnableAsync 启用异步支持
  // @Async 注解的方法将在单独的线程中执行
}