package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RefreshTokenRequest DTO 单元测试
 */
@DisplayName("RefreshTokenRequest DTO 单元测试")
class RefreshTokenRequestTest {

    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        refreshTokenRequest = new RefreshTokenRequest();
    }

    @Test
    @DisplayName("设置和获取刷新 Token")
    void setRefreshTokenAndGetRefreshToken() {
        // Given
        String refreshToken = "test-refresh-token";

        // When
        refreshTokenRequest.setRefreshToken(refreshToken);

        // Then
        assertEquals("test-refresh-token", refreshTokenRequest.getRefreshToken());
    }

    @Test
    @DisplayName("完整 RefreshTokenRequest 对象测试")
    void completeRefreshTokenRequestObject() {
        // Given
        refreshTokenRequest.setRefreshToken("test-refresh-token");

        // Then
        assertEquals("test-refresh-token", refreshTokenRequest.getRefreshToken());
    }
}
