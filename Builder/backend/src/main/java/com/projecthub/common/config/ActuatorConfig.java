package com.projecthub.common.config;

import org.springframework.boot.actuate.mail.MailHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Actuator 配置类 在开发环境下禁用邮件健康检查，避免因为邮件配置不实导致健康检查失败 */
@Configuration
@Profile("dev")
public class ActuatorConfig {

  /** 开发环境下禁用邮件健康检查 因为开发环境邮件配置为示例配置，实际并未配置真实的邮件服务器 */
  @Bean
  public MailHealthIndicator mailHealthIndicator() {
    return null; // 返回 null 表示禁用此健康检查
  }
}
