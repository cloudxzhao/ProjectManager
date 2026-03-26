package com.projecthub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.auth.dto.*;
import com.projecthub.auth.service.AuthService;
import com.projecthub.common.api.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth Controller 单元测试
 */
@DisplayName("AuthController 单元测试")
class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private ObjectMapper objectMapper;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        mockAuthResponse = new AuthResponse();
        mockAuthResponse.setAccessToken("mock-access-token");
        mockAuthResponse.setRefreshToken("mock-refresh-token");
        mockAuthResponse.setExpiresIn(3600L);
        mockAuthResponse.setTokenType("Bearer");

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("MEMBER");
        mockAuthResponse.setUser(userInfo);
    }

    @Test
    @DisplayName("登录成功")
    void login_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.user.id").value(1));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("登录失败 - 邮箱为空")
    void login_Fail_EmailEmpty() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("登录失败 - 密码为空")
    void login_Fail_PasswordEmpty() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("登录失败 - 邮箱格式错误")
    void login_Fail_InvalidEmail() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("注册成功")
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名为空")
    void register_Fail_UsernameEmpty() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("注册失败 - 密码太短")
    void register_Fail_PasswordTooShort() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("short");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("刷新 Token 成功")
    void refreshToken_Success() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(mockAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 为空")
    void refreshToken_Fail_TokenEmpty() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any());
    }

    @Test
    @DisplayName("忘记密码成功")
    void forgotPassword_Success() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("重置邮件已发送，请查收"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    @DisplayName("忘记密码失败 - 邮箱为空")
    void forgotPassword_Fail_EmailEmpty() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).forgotPassword(any());
    }

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_Success() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NewPassword123");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("健康检查")
    void health_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    @DisplayName("退出登录成功")
    void logout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("退出成功"));
    }
}
