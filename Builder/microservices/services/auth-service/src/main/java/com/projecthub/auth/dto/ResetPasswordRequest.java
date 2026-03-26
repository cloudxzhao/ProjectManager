package com.projecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Schema(description = "重置 Token")
    @NotBlank(message = "Token 不能为空")
    private String token;

    @Schema(description = "新密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度为8-20个字符")
    private String newPassword;

}