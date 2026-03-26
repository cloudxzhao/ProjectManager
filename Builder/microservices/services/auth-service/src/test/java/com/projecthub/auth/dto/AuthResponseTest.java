package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthResponse DTO 单元测试
 */
@DisplayName("AuthResponse DTO 单元测试")
class AuthResponseTest {

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse();
    }

    @Test
    @DisplayName("设置和获取访问 Token")
    void setAccessTokenAndGetAccessToken() {
        // Given
        String accessToken = "access-token-123";

        // When
        authResponse.setAccessToken(accessToken);

        // Then
        assertEquals("access-token-123", authResponse.getAccessToken());
    }

    @Test
    @DisplayName("设置和获取刷新 Token")
    void setRefreshTokenAndGetRefreshToken() {
        // Given
        String refreshToken = "refresh-token-123";

        // When
        authResponse.setRefreshToken(refreshToken);

        // Then
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
    }

    @Test
    @DisplayName("设置和获取过期时间")
    void setExpiresInAndGetExpiresIn() {
        // Given
        Long expiresIn = 3600L;

        // When
        authResponse.setExpiresIn(expiresIn);

        // Then
        assertEquals(3600L, authResponse.getExpiresIn());
    }

    @Test
    @DisplayName("设置和获取 Token 类型")
    void setTokenTypeAndGetTokenType() {
        // Given
        String tokenType = "Bearer";

        // When
        authResponse.setTokenType(tokenType);

        // Then
        assertEquals("Bearer", authResponse.getTokenType());
    }

    @Test
    @DisplayName("设置和获取用户信息")
    void setUserAndGetUser() {
        // Given
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("MEMBER");

        // When
        authResponse.setUser(userInfo);

        // Then
        assertNotNull(authResponse.getUser());
        assertEquals(1L, authResponse.getUser().getId());
        assertEquals("testuser", authResponse.getUser().getUsername());
        assertEquals("test@example.com", authResponse.getUser().getEmail());
        assertEquals("MEMBER", authResponse.getUser().getRole());
    }

    @Test
    @DisplayName("完整 AuthResponse 对象测试")
    void completeAuthResponseObject() {
        // Given
        authResponse.setAccessToken("access-token-123");
        authResponse.setRefreshToken("refresh-token-123");
        authResponse.setExpiresIn(3600L);
        authResponse.setTokenType("Bearer");

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("MEMBER");
        authResponse.setUser(userInfo);

        // Then
        assertEquals("access-token-123", authResponse.getAccessToken());
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
        assertEquals(3600L, authResponse.getExpiresIn());
        assertEquals("Bearer", authResponse.getTokenType());
        assertNotNull(authResponse.getUser());
        assertEquals(1L, authResponse.getUser().getId());
        assertEquals("testuser", authResponse.getUser().getUsername());
        assertEquals("test@example.com", authResponse.getUser().getEmail());
        assertEquals("MEMBER", authResponse.getUser().getRole());
    }

    @Test
    @DisplayName("UserInfo 内部类测试")
    void userInfoTest() {
        // Given
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();

        // When
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("ADMIN");

        // Then
        assertEquals(1L, userInfo.getId());
        assertEquals("testuser", userInfo.getUsername());
        assertEquals("test@example.com", userInfo.getEmail());
        assertEquals("ADMIN", userInfo.getRole());
    }
}
