package com.projecthub.module.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.projecthub.common.util.JwtUtil;
import com.projecthub.common.util.PasswordUtil;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

/** 密码工具类单元测试 测试覆盖：密码加密、密码验证 */
@DisplayName("PasswordUtil 单元测试")
@ExtendWith(MockitoExtension.class)
class PasswordUtilTest {

  private PasswordUtil passwordUtil;

  @BeforeEach
  void setUp() {
    passwordUtil = new PasswordUtil();
  }

  @Nested
  @DisplayName("密码加密测试")
  class EncodePasswordTests {

    @Test
    @DisplayName("加密成功 - 不同密码生成不同哈希")
    void encode_DifferentPasswords_GenerateDifferentHashes() {
      // Act
      String hash1 = passwordUtil.encode("Password123");
      String hash2 = passwordUtil.encode("DifferentPassword456");

      // Assert
      assertThat(hash1).isNotEqualTo(hash2);
      assertThat(hash1).startsWith("$2a$10$");
    }

    @Test
    @DisplayName("加密成功 - 相同密码每次生成不同哈希（salt）")
    void encode_SamePassword_GenerateDifferentHashesDueToSalt() {
      // Act
      String hash1 = passwordUtil.encode("Password123");
      String hash2 = passwordUtil.encode("Password123");

      // Assert
      assertThat(hash1).isNotEqualTo(hash2); // Salt makes them different
      assertThat(hash1).startsWith("$2a$10$");
      assertThat(hash2).startsWith("$2a$10$");
    }

    @Test
    @DisplayName("加密成功 - 特殊字符密码")
    void encode_SpecialCharacters_Password() {
      // Act
      String hash = passwordUtil.encode("P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?");

      // Assert
      assertThat(hash).isNotNull();
      assertThat(hash).startsWith("$2a$10$");
    }
  }

  @Nested
  @DisplayName("密码验证测试")
  class MatchPasswordTests {

    @Test
    @DisplayName("验证成功 - 密码正确")
    void matches_CorrectPassword_ReturnsTrue() {
      // Arrange
      String rawPassword = "Password123";
      String encodedPassword = passwordUtil.encode(rawPassword);

      // Act
      boolean result = passwordUtil.matches(rawPassword, encodedPassword);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证失败 - 密码错误")
    void matches_WrongPassword_ReturnsFalse() {
      // Arrange
      String correctPassword = "Password123";
      String wrongPassword = "WrongPassword";
      String encodedPassword = passwordUtil.encode(correctPassword);

      // Act
      boolean result = passwordUtil.matches(wrongPassword, encodedPassword);

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("验证失败 - 空密码")
    void matches_EmptyPassword_ReturnsFalse() {
      // Arrange
      String encodedPassword = passwordUtil.encode("Password123");

      // Act
      boolean result = passwordUtil.matches("", encodedPassword);

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("验证失败 - 空哈希")
    void matches_EmptyHash_ReturnsFalse() {
      // Act
      boolean result = passwordUtil.matches("Password123", "");

      // Assert
      assertThat(result).isFalse();
    }
  }
}

/** JWT工具类单元测试 测试覆盖：Token生成、解析、验证 */
@DisplayName("JwtUtil 单元测试")
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

  private JwtUtil jwtUtil;

  private static final String SECRET =
      "projecthub-secret-key-for-jwt-token-generation-must-be-long-enough";
  private static final long EXPIRATION = 7200000L; // 2小时
  private static final long REFRESH_EXPIRATION = 604800000L; // 7天

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", REFRESH_EXPIRATION);
  }

  @Nested
  @DisplayName("生成Token测试")
  class GenerateTokenTests {

    @Test
    @DisplayName("生成成功 - Access Token")
    void generateToken_Success_ReturnsValidToken() {
      // Act
      String token = jwtUtil.generateToken("testuser", 1L);

      // Assert
      assertThat(token).isNotNull();
      assertThat(token.split("\\.")).hasSize(3); // JWT格式: header.payload.signature
    }

    @Test
    @DisplayName("生成成功 - Refresh Token")
    void generateRefreshToken_Success_ReturnsValidToken() {
      // Act
      String token = jwtUtil.generateRefreshToken("testuser", 1L);

      // Assert
      assertThat(token).isNotNull();
      assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("生成成功 - 不同用户生成不同Token")
    void generateToken_DifferentUsers_GenerateDifferentTokens() {
      // Act
      String token1 = jwtUtil.generateToken("user1", 1L);
      String token2 = jwtUtil.generateToken("user2", 2L);

      // Assert
      assertThat(token1).isNotEqualTo(token2);
    }
  }

  @Nested
  @DisplayName("解析Token测试")
  class ParseTokenTests {

    @Test
    @DisplayName("解析成功 - 获取用户名")
    void getUsernameFromToken_Success_ReturnsUsername() {
      // Arrange
      String token = jwtUtil.generateToken("testuser", 1L);

      // Act
      String username = jwtUtil.getUsernameFromToken(token);

      // Assert
      assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("解析成功 - 获取用户ID")
    void getUserIdFromToken_Success_ReturnsUserId() {
      // Arrange
      String token = jwtUtil.generateToken("testuser", 1L);

      // Act
      Long userId = jwtUtil.getUserIdFromToken(token);

      // Assert
      assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("解析成功 - 获取过期时间")
    void getExpirationDate_Success_ReturnsExpirationDate() {
      // Arrange
      String token = jwtUtil.generateToken("testuser", 1L);

      // Act
      java.util.Date expiration = jwtUtil.getExpirationDate(token);

      // Assert
      assertThat(expiration).isAfter(new java.util.Date());
    }
  }

  @Nested
  @DisplayName("验证Token测试")
  class ValidateTokenTests {

    @Test
    @DisplayName("验证成功 - 有效Token")
    void validateToken_ValidToken_ReturnsTrue() {
      // Arrange
      String token = jwtUtil.generateToken("testuser", 1L);

      UserDetailsImpl userDetails =
          new UserDetailsImpl(
              1L,
              "testuser",
              "test@test.com",
              "hash",
              List.of(new SimpleGrantedAuthority("ROLE_USER")),
              true,
              true,
              true,
              true);

      // Act
      boolean result = jwtUtil.validateToken(token, userDetails);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证失败 - 用户名不匹配")
    void validateToken_UsernameMismatch_ReturnsFalse() {
      // Arrange
      String token = jwtUtil.generateToken("testuser", 1L);

      UserDetailsImpl userDetails =
          new UserDetailsImpl(
              1L,
              "differentuser", // 不同用户名
              "test@test.com",
              "hash",
              List.of(new SimpleGrantedAuthority("ROLE_USER")),
              true,
              true,
              true,
              true);

      // Act
      boolean result = jwtUtil.validateToken(token, userDetails);

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("验证失败 - 无效Token")
    void validateToken_InvalidToken_ReturnsFalse() {
      // Arrange
      String invalidToken = "invalid.token.here";

      UserDetailsImpl userDetails =
          new UserDetailsImpl(
              1L,
              "testuser",
              "test@test.com",
              "hash",
              List.of(new SimpleGrantedAuthority("ROLE_USER")),
              true,
              true,
              true,
              true);

      // Act
      boolean result = jwtUtil.validateToken(invalidToken, userDetails);

      // Assert
      assertThat(result).isFalse();
    }
  }
}
