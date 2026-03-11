package com.projecthub.module.auth.controller;

import com.projecthub.common.response.Result;
import com.projecthub.module.auth.dto.AuthResponse;
import com.projecthub.module.auth.dto.ForgotPasswordRequest;
import com.projecthub.module.auth.dto.LoginRequest;
import com.projecthub.module.auth.dto.RefreshTokenRequest;
import com.projecthub.module.auth.dto.RegisterRequest;
import com.projecthub.module.auth.dto.ResetPasswordRequest;
import com.projecthub.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 认证控制器 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

  private final AuthService authService;

  /** 用户登录 */
  @PostMapping("/login")
  @Operation(summary = "用户登录", description = "使用用户名/邮箱和密码登录")
  public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return Result.success(response);
  }

  /** 用户注册 */
  @PostMapping("/register")
  @Operation(summary = "用户注册", description = "创建新用户账号")
  public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return Result.success("注册成功", response);
  }

  /** 刷新 Token */
  @PostMapping("/refresh")
  @Operation(summary = "刷新 Token", description = "使用刷新 Token 获取新的访问 Token")
  public Result<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse response = authService.refreshToken(request.getRefreshToken());
    return Result.success(response);
  }

  /** 用户登出 */
  @PostMapping("/logout")
  @Operation(summary = "用户登出", description = "使当前 Token 失效")
  public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
    String token = extractToken(authorization);
    authService.logout(token);
    return Result.success();
  }

  /** 忘记密码 - 发送重置邮件 */
  @PostMapping("/forgot-password")
  @Operation(summary = "忘记密码", description = "发送密码重置邮件到用户邮箱")
  public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.getEmail());
    return Result.success("密码重置邮件已发送，请检查邮箱");
  }

  /** 重置密码 */
  @PostMapping("/reset-password")
  @Operation(summary = "重置密码", description = "使用重置 Token 设置新密码")
  public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.getResetToken(), request.getNewPassword());
    return Result.success("密码重置成功");
  }

  /** 从 Authorization header 中提取 Token */
  private String extractToken(String authorization) {
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }
    return authorization;
  }
}
