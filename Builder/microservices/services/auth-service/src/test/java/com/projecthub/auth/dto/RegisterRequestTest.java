package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegisterRequest DTO 单元测试
 */
@DisplayName("RegisterRequest DTO 单元测试")
class RegisterRequestTest {

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
    }

    @Test
    @DisplayName("设置和获取用户名")
    void setUsernameAndGetUsername() {
        // Given
        String username = "testuser";

        // When
        registerRequest.setUsername(username);

        // Then
        assertEquals("testuser", registerRequest.getUsername());
    }

    @Test
    @DisplayName("设置和获取邮箱")
    void setEmailAndGetEmail() {
        // Given
        String email = "test@example.com";

        // When
        registerRequest.setEmail(email);

        // Then
        assertEquals("test@example.com", registerRequest.getEmail());
    }

    @Test
    @DisplayName("设置和获取密码")
    void setPasswordAndGetPassword() {
        // Given
        String password = "Password123";

        // When
        registerRequest.setPassword(password);

        // Then
        assertEquals("Password123", registerRequest.getPassword());
    }

    @Test
    @DisplayName("完整 RegisterRequest 对象测试")
    void completeRegisterRequestObject() {
        // Given
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");

        // Then
        assertEquals("testuser", registerRequest.getUsername());
        assertEquals("test@example.com", registerRequest.getEmail());
        assertEquals("Password123", registerRequest.getPassword());
    }
}
