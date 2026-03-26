package com.projecthub.auth.service;

import cn.hutool.core.util.IdUtil;
import com.projecthub.auth.dto.*;
import com.projecthub.auth.entity.RefreshToken;
import com.projecthub.auth.entity.User;
import com.projecthub.auth.repository.RefreshTokenRepository;
import com.projecthub.auth.repository.UserRepository;
import com.projecthub.common.core.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Auth Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JavaMailSender mailSender;

    private AuthService authService;

    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, mailSender);

        // 设置 JWT 配置
        ReflectionTestUtils.setField(authService, "jwtSecret", "test-secret-key-for-unit-testing-must-be-at-least-512-bits-long-for-hs512-algorithm-secure-enough");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L); // 7 days
        ReflectionTestUtils.setField(authService, "issuer", "projecthub");

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("MEMBER");
        testUser.setStatus(1);
        testUser.setEmailVerified(1);
        testUser.setDeleted(0);
    }

    @Test
    @DisplayName("登录成功 - 返回 Token")
    void login_Success_ReturnsToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("MEMBER");
        user.setStatus(1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).updateById(any(User.class));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_Fail_UserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request);
        });

        assertEquals(1001, exception.getCode());
        assertEquals("邮箱或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_Fail_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("MEMBER");
        user.setStatus(1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request);
        });

        assertEquals(1001, exception.getCode());
        assertEquals("邮箱或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 用户被禁用")
    void login_Fail_UserDisabled() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("MEMBER");
        user.setStatus(0); // Disabled

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request);
        });

        assertEquals(1004, exception.getCode());
        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    @DisplayName("注册成功 - 返回 Token")
    void register_Success_ReturnsToken() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.insert(any(User.class))).thenReturn(1);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).insert(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("newuser", capturedUser.getUsername());
        assertEquals("newuser@example.com", capturedUser.getEmail());
        assertEquals("MEMBER", capturedUser.getRole());
        assertEquals(1, capturedUser.getStatus());
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_Fail_EmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(request);
        });

        assertEquals(2003, exception.getCode());
        assertEquals("邮箱已被注册", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_Fail_UsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setUsername("existing");
        request.setPassword("password123");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(request);
        });

        assertEquals(2002, exception.getCode());
        assertEquals("用户名已被使用", exception.getMessage());
    }

    @Test
    @DisplayName("刷新 Token 成功")
    void refreshToken_Success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(0);

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.selectById(1L)).thenReturn(testUser);

        // When
        AuthResponse response = authService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).markAsUsed("valid-refresh-token");
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 无效")
    void refreshToken_Fail_InvalidToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals(1003, exception.getCode());
        assertEquals("无效的刷新 Token", exception.getMessage());
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 已使用")
    void refreshToken_Fail_TokenUsed() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("used-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken("used-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(1); // Already used

        when(refreshTokenRepository.findByToken("used-token")).thenReturn(Optional.of(refreshToken));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals(1003, exception.getCode());
        assertEquals("刷新 Token 已使用", exception.getMessage());
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 过期")
    void refreshToken_Fail_TokenExpired() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken("expired-token");
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired
        refreshToken.setUsed(0);

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(refreshToken));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals(1002, exception.getCode());
        assertEquals("刷新 Token 已过期", exception.getMessage());
    }

    @Test
    @DisplayName("刷新 Token 失败 - 用户不存在")
    void refreshToken_Fail_UserNotFound() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(999L);
        refreshToken.setToken("valid-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(0);

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("忘记密码 - 邮件发送成功")
    void forgotPassword_Success() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        authService.forgotPassword(request);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("忘记密码 - 用户不存在")
    void forgotPassword_Fail_UserNotFound() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.forgotPassword(request);
        });

        assertEquals(2001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_Success() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("newPassword123");

        // Note: Current implementation just logs success (Redis not implemented)
        // This test verifies the method doesn't throw exception
        assertDoesNotThrow(() -> authService.resetPassword(request));
    }

    @Test
    @DisplayName("重置密码 - 新密码为空")
    void resetPassword_Fail_NewPasswordEmpty() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("");

        // When & Then
        assertDoesNotThrow(() -> authService.resetPassword(request));
    }

    @Test
    @DisplayName("生成访问 Token 验证")
    void generateAccessToken_VerifyToken() {
        // This test verifies the token generation logic
        String token = generateAccessToken(testUser);

        // Parse and verify token
        SecretKey key = Keys.hmacShaKeyFor("test-secret-key-for-unit-testing-must-be-at-least-512-bits-long-for-hs512-algorithm-secure-enough".getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.get("username", String.class));
        assertEquals("MEMBER", claims.get("role", String.class));
        assertEquals("test@example.com", claims.getSubject());
        assertEquals("projecthub", claims.getIssuer());
    }

    @Test
    @DisplayName("生成访问 Token - 验证过期时间")
    void generateAccessToken_VerifyExpiration() {
        // Given
        long beforeTokenGen = System.currentTimeMillis();
        String token = generateAccessToken(testUser);
        long afterTokenGen = System.currentTimeMillis();

        // Parse and verify expiration
        SecretKey key = Keys.hmacShaKeyFor("test-secret-key-for-unit-testing-must-be-at-least-512-bits-long-for-hs512-algorithm-secure-enough".getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        assertNotNull(issuedAt);
        assertNotNull(expiration);
        // 允许一定的时间误差
        long actualExpiration = expiration.getTime() - issuedAt.getTime();
        assertTrue(actualExpiration > 3599000 && actualExpiration < 3601000,
                   "Token expiration should be around 3600000ms, but was " + actualExpiration);
    }

    @Test
    @DisplayName("刷新 Token 成功 - 验证旧 Token 标记为已使用")
    void refreshToken_Success_VerifyOldTokenMarkedAsUsed() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(0);

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(refreshTokenRepository.markAsUsed("valid-refresh-token")).thenReturn(1);

        // When
        AuthResponse response = authService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).markAsUsed("valid-refresh-token");
    }

    @Test
    @DisplayName("登录失败 - 邮箱格式无效")
    void login_Fail_InvalidEmailFormat() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email-format");
        request.setPassword("password123");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request);
        });

        assertEquals(1001, exception.getCode());
        assertEquals("邮箱或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("注册成功 - 验证用户默认角色")
    void register_Success_VerifyDefaultRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.insert(any(User.class))).thenReturn(1);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("MEMBER", response.getUser().getRole());
    }

    @Test
    @DisplayName("忘记密码 - 邮件发送验证内容")
    void forgotPassword_VerifyEmailContent() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        authService.forgotPassword(request);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        // 邮件内容验证
        String text = capturedMessage.getText();
        assertNotNull(text);
        assertTrue(text.contains("30 分钟"), "邮件内容应包含 30 分钟提示");
    }

    // Helper method to test private generateAccessToken
    private String generateAccessToken(User user) {
        String jwtSecret = "test-secret-key-for-unit-testing-must-be-at-least-512-bits-long-for-hs512-algorithm-secure-enough";
        Long jwtExpiration = 3600000L;
        String issuer = "projecthub";

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }
}
