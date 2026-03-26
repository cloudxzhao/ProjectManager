package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResetPasswordRequest DTO 单元测试
 */
@DisplayName("ResetPasswordRequest DTO 单元测试")
class ResetPasswordRequestTest {

    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        resetPasswordRequest = new ResetPasswordRequest();
    }

    @Test
    @DisplayName("设置和获取 Token")
    void setTokenAndGetToken() {
        // Given
        String token = "valid-reset-token";

        // When
        resetPasswordRequest.setToken(token);

        // Then
        assertEquals("valid-reset-token", resetPasswordRequest.getToken());
    }

    @Test
    @DisplayName("设置和获取新密码")
    void setNewPasswordAndGetNewPassword() {
        // Given
        String newPassword = "NewPassword123";

        // When
        resetPasswordRequest.setNewPassword(newPassword);

        // Then
        assertEquals("NewPassword123", resetPasswordRequest.getNewPassword());
    }

    @Test
    @DisplayName("完整 ResetPasswordRequest 对象测试")
    void completeResetPasswordRequestObject() {
        // Given
        resetPasswordRequest.setToken("valid-reset-token");
        resetPasswordRequest.setNewPassword("NewPassword123");

        // Then
        assertEquals("valid-reset-token", resetPasswordRequest.getToken());
        assertEquals("NewPassword123", resetPasswordRequest.getNewPassword());
    }
}
