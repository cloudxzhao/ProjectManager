package com.projecthub.auth.controller;

import com.projecthub.auth.dto.*;
import com.projecthub.auth.service.AuthService;
import com.projecthub.common.api.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、注册、Token 管理接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用邮箱和密码登录")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新 Token", description = "使用刷新 Token 获取新的访问 Token")
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success("刷新成功", authService.refreshToken(request));
    }

    /**
     * 忘记密码
     */
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Result.success("重置邮件已发送，请查收", null);
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码", description = "通过重置 Token 设置新密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "退出登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 清除 Token 缓存
        return Result.success("退出成功", null);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }

}