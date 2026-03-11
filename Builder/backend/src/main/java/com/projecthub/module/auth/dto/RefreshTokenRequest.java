package com.projecthub.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 刷新 Token 请求 DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

  /** 刷新 Token */
  @NotBlank(message = "刷新 Token 不能为空")
  private String refreshToken;
}
