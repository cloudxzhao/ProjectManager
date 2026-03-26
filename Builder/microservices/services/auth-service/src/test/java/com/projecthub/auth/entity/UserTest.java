package com.projecthub.auth.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Entity 单元测试
 */
@DisplayName("User Entity 单元测试")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("设置和获取 ID")
    void setIdAndGetId() {
        // Given
        Long id = 1L;

        // When
        user.setId(id);

        // Then
        assertEquals(1L, user.getId());
    }

    @Test
    @DisplayName("设置和获取用户名")
    void setUsernameAndGetUsername() {
        // Given
        String username = "testuser";

        // When
        user.setUsername(username);

        // Then
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("设置和获取邮箱")
    void setEmailAndGetEmail() {
        // Given
        String email = "test@example.com";

        // When
        user.setEmail(email);

        // Then
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("设置和获取密码")
    void setPasswordAndGetPassword() {
        // Given
        String password = "encoded-password";

        // When
        user.setPassword(password);

        // Then
        assertEquals("encoded-password", user.getPassword());
    }

    @Test
    @DisplayName("设置和获取手机号")
    void setPhoneAndGetPhone() {
        // Given
        String phone = "13800138000";

        // When
        user.setPhone(phone);

        // Then
        assertEquals("13800138000", user.getPhone());
    }

    @Test
    @DisplayName("设置和获取头像")
    void setAvatarAndGetAvatar() {
        // Given
        String avatar = "https://example.com/avatar.jpg";

        // When
        user.setAvatar(avatar);

        // Then
        assertEquals("https://example.com/avatar.jpg", user.getAvatar());
    }

    @Test
    @DisplayName("设置和获取角色")
    void setRoleAndGetRole() {
        // Given
        String role = "ADMIN";

        // When
        user.setRole(role);

        // Then
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    @DisplayName("设置和获取状态")
    void setStatusAndGetStatus() {
        // Given
        Integer status = 1;

        // When
        user.setStatus(status);

        // Then
        assertEquals(1, user.getStatus());
    }

    @Test
    @DisplayName("设置和获取邮箱验证状态")
    void setEmailVerifiedAndGetEmailVerified() {
        // Given
        Integer emailVerified = 1;

        // When
        user.setEmailVerified(emailVerified);

        // Then
        assertEquals(1, user.getEmailVerified());
    }

    @Test
    @DisplayName("设置和获取最后登录时间")
    void setLastLoginAtAndGetLastLoginAt() {
        // Given
        LocalDateTime lastLoginAt = LocalDateTime.now();

        // When
        user.setLastLoginAt(lastLoginAt);

        // Then
        assertEquals(lastLoginAt, user.getLastLoginAt());
    }

    @Test
    @DisplayName("设置和获取创建时间")
    void setCreatedAtAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        user.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    @DisplayName("设置和获取更新时间")
    void setUpdatedAtAndGetUpdatedAt() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        user.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    @DisplayName("设置和获取删除状态")
    void setDeletedAndGetDeleted() {
        // Given
        Integer deleted = 0;

        // When
        user.setDeleted(deleted);

        // Then
        assertEquals(0, user.getDeleted());
    }

    @Test
    @DisplayName("完整用户对象测试")
    void completeUserObject() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setPhone("13800138000");
        user.setAvatar("https://example.com/avatar.jpg");
        user.setRole("ADMIN");
        user.setStatus(1);
        user.setEmailVerified(1);
        user.setLastLoginAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(0);

        // Then
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encoded-password", user.getPassword());
        assertEquals("13800138000", user.getPhone());
        assertEquals("https://example.com/avatar.jpg", user.getAvatar());
        assertEquals("ADMIN", user.getRole());
        assertEquals(1, user.getStatus());
        assertEquals(1, user.getEmailVerified());
        assertEquals(now, user.getLastLoginAt());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(0, user.getDeleted());
    }
}
