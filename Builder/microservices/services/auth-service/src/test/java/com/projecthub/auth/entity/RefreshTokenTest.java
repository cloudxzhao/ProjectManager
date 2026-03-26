package com.projecthub.auth.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RefreshToken Entity 单元测试
 */
@DisplayName("RefreshToken Entity 单元测试")
class RefreshTokenTest {

    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshToken = new RefreshToken();
    }

    @Test
    @DisplayName("设置和获取 ID")
    void setIdAndGetId() {
        // Given
        Long id = 1L;

        // When
        refreshToken.setId(id);

        // Then
        assertEquals(1L, refreshToken.getId());
    }

    @Test
    @DisplayName("设置和获取用户 ID")
    void setUserIdAndGetUserId() {
        // Given
        Long userId = 1L;

        // When
        refreshToken.setUserId(userId);

        // Then
        assertEquals(1L, refreshToken.getUserId());
    }

    @Test
    @DisplayName("设置和获取 Token")
    void setTokenAndGetToken() {
        // Given
        String token = "test-refresh-token";

        // When
        refreshToken.setToken(token);

        // Then
        assertEquals("test-refresh-token", refreshToken.getToken());
    }

    @Test
    @DisplayName("设置和获取过期时间")
    void setExpiresAtAndGetExpiresAt() {
        // Given
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        // When
        refreshToken.setExpiresAt(expiresAt);

        // Then
        assertEquals(expiresAt, refreshToken.getExpiresAt());
    }

    @Test
    @DisplayName("设置和获取使用状态")
    void setUsedAndGetUsed() {
        // Given
        Integer used = 1;

        // When
        refreshToken.setUsed(used);

        // Then
        assertEquals(1, refreshToken.getUsed());
    }

    @Test
    @DisplayName("设置和获取创建时间")
    void setCreatedAtAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        refreshToken.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, refreshToken.getCreatedAt());
    }

    @Test
    @DisplayName("完整 RefreshToken 对象测试")
    void completeRefreshTokenObject() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken("test-refresh-token");
        refreshToken.setExpiresAt(now.plusDays(7));
        refreshToken.setUsed(0);
        refreshToken.setCreatedAt(now);

        // Then
        assertEquals(1L, refreshToken.getId());
        assertEquals(1L, refreshToken.getUserId());
        assertEquals("test-refresh-token", refreshToken.getToken());
        assertEquals(now.plusDays(7), refreshToken.getExpiresAt());
        assertEquals(0, refreshToken.getUsed());
        assertEquals(now, refreshToken.getCreatedAt());
    }

    @Test
    @DisplayName("Token 过期检查 - 未过期")
    void isNotExpired() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);
        refreshToken.setExpiresAt(futureTime);

        // Then
        assertFalse(refreshToken.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Token 过期检查 - 已过期")
    void isExpired() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        refreshToken.setExpiresAt(pastTime);

        // Then
        assertTrue(refreshToken.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Token 使用状态检查 - 已使用")
    void isUsed() {
        // Given
        refreshToken.setUsed(1);

        // Then
        assertEquals(1, refreshToken.getUsed());
    }

    @Test
    @DisplayName("Token 使用状态检查 - 未使用")
    void isUnused() {
        // Given
        refreshToken.setUsed(0);

        // Then
        assertEquals(0, refreshToken.getUsed());
    }
}
