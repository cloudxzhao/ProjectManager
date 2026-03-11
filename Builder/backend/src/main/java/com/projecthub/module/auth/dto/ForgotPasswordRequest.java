package com.projecthub.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 忘记密码请求 DTO */
@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  @Schema(description = "用户邮箱", example = "user@example.com", required = true)
  private String email;
}
