package com.projecthub.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 重置密码请求 DTO */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

  @NotBlank(message = "重置 Token 不能为空")
  @Schema(description = "重置 Token", example = "abc123...", required = true)
  private String resetToken;

  @NotBlank(message = "新密码不能为空")
  @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
  @Schema(description = "新密码", example = "newPassword123", required = true)
  private String newPassword;
}
