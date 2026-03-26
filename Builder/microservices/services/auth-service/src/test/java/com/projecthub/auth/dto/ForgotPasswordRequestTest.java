package com.projecthub.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ForgotPasswordRequest DTO 单元测试
 */
@DisplayName("ForgotPasswordRequest DTO 单元测试")
class ForgotPasswordRequestTest {

    private ForgotPasswordRequest forgotPasswordRequest;

    @BeforeEach
    void setUp() {
        forgotPasswordRequest = new ForgotPasswordRequest();
    }

    @Test
    @DisplayName("设置和获取邮箱")
    void setEmailAndGetEmail() {
        // Given
        String email = "test@example.com";

        // When
        forgotPasswordRequest.setEmail(email);

        // Then
        assertEquals("test@example.com", forgotPasswordRequest.getEmail());
    }

    @Test
    @DisplayName("完整 ForgotPasswordRequest 对象测试")
    void completeForgotPasswordRequestObject() {
        // Given
        forgotPasswordRequest.setEmail("test@example.com");

        // Then
        assertEquals("test@example.com", forgotPasswordRequest.getEmail());
    }
}
