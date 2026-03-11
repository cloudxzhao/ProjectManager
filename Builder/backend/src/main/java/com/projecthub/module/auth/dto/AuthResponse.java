package com.projecthub.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 认证响应 DTO (登录/注册/刷新 Token) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  /** 访问 Token */
  private String accessToken;

  /** 刷新 Token */
  private String refreshToken;

  /** Token 类型 */
  @Builder.Default private String tokenType = "Bearer";

  /** 过期时间 (毫秒) */
  private Long expiresIn;
}
