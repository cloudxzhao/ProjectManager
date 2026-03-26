package com.projecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求
 */
@Data
@Schema(description = "刷新 Token 请求")
public class RefreshTokenRequest {

    @Schema(description = "刷新 Token")
    @NotBlank(message = "刷新 Token 不能为空")
    private String refreshToken;

}