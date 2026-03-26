package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginRequest DTO 单元测试
 */
@DisplayName("LoginRequest DTO 单元测试")
class LoginRequestTest {

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
    }

    @Test
    @DisplayName("设置和获取邮箱")
    void setEmailAndGetEmail() {
        // Given
        String email = "test@example.com";

        // When
        loginRequest.setEmail(email);

        // Then
        assertEquals("test@example.com", loginRequest.getEmail());
    }

    @Test
    @DisplayName("设置和获取密码")
    void setPasswordAndGetPassword() {
        // Given
        String password = "password123";

        // When
        loginRequest.setPassword(password);

        // Then
        assertEquals("password123", loginRequest.getPassword());
    }

    @Test
    @DisplayName("完整 LoginRequest 对象测试")
    void completeLoginRequestObject() {
        // Given
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Then
        assertEquals("test@example.com", loginRequest.getEmail());
        assertEquals("password123", loginRequest.getPassword());
    }
}
