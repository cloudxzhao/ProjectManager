package com.projecthub.module.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.JwtUtil;
import com.projecthub.common.util.PasswordUtil;
import com.projecthub.infrastructure.email.EmailService;
import com.projecthub.module.auth.service.AuthService;
import com.projecthub.module.auth.dto.AuthResponse;
import com.projecthub.module.auth.dto.LoginRequest;
import com.projecthub.module.auth.dto.RegisterRequest;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/** 认证服务单元测试 测试覆盖：登录、注册、Token刷新、登出、密码重置 */
@DisplayName("AuthService 单元测试")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtUtil jwtUtil;

  @Mock private PasswordUtil passwordUtil;

  @Mock private UserRepository userRepository;

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private EmailService emailService;

  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private AuthService authService;

  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;
  private User testUser;
  private UserDetailsImpl userDetails;

  @BeforeEach
  void setUp() {
    // 初始化登录请求
    loginRequest = new LoginRequest("testuser", "Password123");

    // 初始化注册请求
    registerRequest = new RegisterRequest("newuser", "newuser@test.com", "Password123");

    // 初始化测试用户
    testUser =
        new User(
            1L,
            "testuser",
            "testuser@test.com",
            "$2a$10$hashedpassword",
            null,
            User.UserStatus.ACTIVE,
            null,
            null,
            null);

    // 初始化用户详情
    userDetails = UserDetailsImpl.create(testUser);
  }

  @Nested
  @DisplayName("登录功能测试")
  class LoginTests {

    @Test
    @DisplayName("登录成功 - 返回正确的令牌响应")
    void login_Success_ReturnsAuthResponse() {
      // Arrange
      Authentication authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenReturn(authentication);
      when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("accessToken");
      when(jwtUtil.generateRefreshToken(anyString(), anyLong())).thenReturn("refreshToken");
      when(jwtUtil.getExpirationDate(anyString()))
          .thenReturn(new java.util.Date(System.currentTimeMillis() + 7200000));

      // Act
      AuthResponse response = authService.login(loginRequest);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo("accessToken");
      assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
      assertThat(response.getTokenType()).isEqualTo("Bearer");
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(jwtUtil).generateToken("testuser", 1L);
    }

    @Test
    @DisplayName("登录失败 - 密码错误抛出异常")
    void login_Failure_WrongPassword_ThrowsException() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(new BadCredentialsException("Invalid credentials"));

      // Act & Assert
      assertThatThrownBy(() -> authService.login(loginRequest))
          .isInstanceOf(BusinessException.class)
          .hasMessage("用户名或密码错误");

      verify(jwtUtil, never()).generateToken(anyString(), anyLong());
    }
  }

  @Nested
  @DisplayName("注册功能测试")
  class RegisterTests {

    @Test
    @DisplayName("注册成功 - 用户不存在且邮箱可用")
    void register_Success_ReturnsAuthResponse() {
      // Arrange
      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
      when(passwordUtil.encode("Password123")).thenReturn("$2a$10$newhashedpassword");
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("accessToken");
      when(jwtUtil.generateRefreshToken(anyString(), anyLong())).thenReturn("refreshToken");
      when(jwtUtil.getExpirationDate(anyString()))
          .thenReturn(new java.util.Date(System.currentTimeMillis() + 7200000));

      // Act
      AuthResponse response = authService.register(registerRequest);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo("accessToken");
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_Failure_UsernameExists_ThrowsException() {
      // Arrange
      when(userRepository.existsByUsername("newuser")).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> authService.register(registerRequest))
          .isInstanceOf(BusinessException.class)
          .hasMessage("用户名已存在");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已被注册")
    void register_Failure_EmailExists_ThrowsException() {
      // Arrange
      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@test.com")).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> authService.register(registerRequest))
          .isInstanceOf(BusinessException.class)
          .hasMessage("邮箱已被注册");

      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("登出功能测试")
  class LogoutTests {

    @Test
    @DisplayName("登出成功 - Token加入黑名单")
    void logout_Success_AddsTokenToBlacklist() {
      // Arrange
      String accessToken = "testAccessToken";
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(jwtUtil.getExpirationDate(accessToken))
          .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

      // Act
      authService.logout(accessToken);

      // Assert
      verify(redisTemplate.opsForValue())
          .set(
              org.mockito.ArgumentMatchers.startsWith("token:blacklist:"),
              org.mockito.eq("logout"),
              org.mockito.anyLong(),
              org.mockito.eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("登出成功 - 空Token不处理")
    void logout_Success_NullToken_DoesNothing() {
      // Act
      authService.logout(null);

      // Assert
      verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("登出成功 - 空字符串Token不处理")
    void logout_Success_EmptyToken_DoesNothing() {
      // Act
      authService.logout("");

      // Assert
      verify(redisTemplate, never()).opsForValue();
    }
  }

  @Nested
  @DisplayName("Token刷新功能测试")
  class RefreshTokenTests {

    @Test
    @DisplayName("刷新成功 - 返回新Token")
    void refreshToken_Success_ReturnsNewTokens() {
      // Arrange
      String refreshToken = "testRefreshToken";
      when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn("testuser");
      when(authService.isTokenBlacklisted(refreshToken)).thenReturn(false);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("newAccessToken");
      when(jwtUtil.generateRefreshToken(anyString(), anyLong())).thenReturn("newRefreshToken");
      when(jwtUtil.getExpirationDate(anyString()))
          .thenReturn(new java.util.Date(System.currentTimeMillis() + 7200000));

      // Act
      AuthResponse response = authService.refreshToken(refreshToken);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
      assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("刷新失败 - Token在黑名单中")
    void refreshToken_Failure_TokenBlacklisted_ThrowsException() {
      // Arrange
      String refreshToken = "blacklistedToken";
      when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn("testuser");
      when(authService.isTokenBlacklisted(refreshToken)).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> authService.refreshToken(refreshToken))
          .isInstanceOf(BusinessException.class)
          .hasMessage("Token 已失效");
    }
  }

  @Nested
  @DisplayName("密码重置功能测试")
  class ForgotPasswordTests {

    @Test
    @DisplayName("密码重置邮件发送成功")
    void forgotPassword_Success_SendsEmail() {
      // Arrange
      String email = "testuser@test.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);

      // Act
      authService.forgotPassword(email);

      // Assert
      verify(emailService)
          .sendPasswordResetEmail(
              org.mockito.Matchers.any(), org.mockito.Matchers.any(), org.mockito.Matchers.any());
    }

    @Test
    @DisplayName("密码重置失败 - 邮箱不存在")
    void forgotPassword_Failure_EmailNotFound_ThrowsException() {
      // Arrange
      String email = "notfound@test.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.forgotPassword(email))
          .isInstanceOf(BusinessException.class)
          .hasMessage("该邮箱未注册");
    }
  }
}
