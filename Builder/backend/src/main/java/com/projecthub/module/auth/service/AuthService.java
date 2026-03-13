package com.projecthub.module.auth.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.JwtUtil;
import com.projecthub.common.util.PasswordUtil;
import com.projecthub.infrastructure.email.EmailService;
import com.projecthub.module.auth.dto.AuthResponse;
import com.projecthub.module.auth.dto.LoginRequest;
import com.projecthub.module.auth.dto.RegisterRequest;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 认证服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final PasswordUtil passwordUtil;
  private final UserRepository userRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final EmailService emailService;

  /** Token 黑名单前缀 */
  private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

  /** 密码重置 Token 前缀和过期时间（1 小时） */
  private static final String PASSWORD_RESET_PREFIX = "password:reset:";

  private static final long PASSWORD_RESET_TTL_HOURS = 1;

  /** 用户登录 */
  @Transactional
  public AuthResponse login(LoginRequest request) {
    try {
      // 认证
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  request.getUsernameOrEmail(), request.getPassword()));

      // 获取用户信息
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      // 生成 Token
      String accessToken = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getId());
      String refreshToken =
          jwtUtil.generateRefreshToken(userDetails.getUsername(), userDetails.getId());

      log.info("用户登录成功：{}", userDetails.getUsername());

      return AuthResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(jwtUtil.getExpirationDate(accessToken).getTime() - System.currentTimeMillis())
          .build();

    } catch (BadCredentialsException e) {
      log.warn("登录失败：用户名或密码错误");
      throw new BusinessException("用户名或密码错误");
    }
  }

  /** 用户注册 */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // 检查用户名是否存在
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new BusinessException("用户名已存在");
    }

    // 检查邮箱是否存在
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("邮箱已被注册");
    }

    // 加密密码
    String encodedPassword = passwordUtil.encode(request.getPassword());

    // 创建用户
    User user =
        User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(encodedPassword)
            .status(User.UserStatus.ACTIVE)
            .build();

    userRepository.save(user);
    log.info("用户注册成功：{}", request.getUsername());

    // 生成 Token
    String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());
    String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtUtil.getExpirationDate(accessToken).getTime() - System.currentTimeMillis())
        .build();
  }

  /** 刷新 Token */
  @Transactional
  public AuthResponse refreshToken(String refreshToken) {
    try {
      // 验证刷新 Token
      String username = jwtUtil.getUsernameFromToken(refreshToken);

      // 检查 Token 是否在黑名单中
      if (isTokenBlacklisted(refreshToken)) {
        throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token 已失效");
      }

      User user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      // 生成新的 Token
      String newAccessToken = jwtUtil.generateToken(username, user.getId());
      String newRefreshToken = jwtUtil.generateRefreshToken(username, user.getId());

      log.info("Token 刷新成功：{}", username);

      return AuthResponse.builder()
          .accessToken(newAccessToken)
          .refreshToken(newRefreshToken)
          .tokenType("Bearer")
          .expiresIn(
              jwtUtil.getExpirationDate(newAccessToken).getTime() - System.currentTimeMillis())
          .build();

    } catch (BusinessException e) {
      throw e; // 重新抛出业务异常
    } catch (Exception e) {
      log.error("刷新 Token 失败：{}", e.getMessage());
      throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token 无效或已过期");
    }
  }

  /** 用户登出 */
  @Transactional
  public void logout(String accessToken) {
    try {
      // 将 Token 加入黑名单
      if (accessToken != null && !accessToken.isEmpty()) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + accessToken;
        long ttl = jwtUtil.getExpirationDate(accessToken).getTime() - System.currentTimeMillis();
        if (ttl > 0) {
          redisTemplate.opsForValue().set(blacklistKey, "logout", ttl, TimeUnit.MILLISECONDS);
        }
      }
      log.info("用户登出成功");
    } catch (Exception e) {
      log.error("登出失败：{}", e.getMessage());
      throw new BusinessException("登出失败");
    }
  }

  /** 检查 Token 是否在黑名单中 */
  public boolean isTokenBlacklisted(String token) {
    String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
  }

  /** 发送密码重置邮件 */
  @Transactional
  public void forgotPassword(String email) {
    // 查找用户
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("该邮箱未注册"));

    // 检查用户状态
    if (user.getStatus() != User.UserStatus.ACTIVE) {
      throw new BusinessException("用户账号已被禁用");
    }

    // 生成重置 Token（32 字节随机数）
    String resetToken = generateResetToken();

    // 将 Token 存储到 Redis，1 小时过期
    String resetKey = PASSWORD_RESET_PREFIX + resetToken;
    redisTemplate
        .opsForValue()
        .set(resetKey, user.getId().toString(), PASSWORD_RESET_TTL_HOURS, TimeUnit.HOURS);

    log.info("为用户 {} 生成密码重置 Token", user.getUsername());

    // 发送重置邮件
    emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getUsername());
  }

  /** 重置密码 */
  @Transactional
  public void resetPassword(String resetToken, String newPassword) {
    // 从 Redis 获取 Token 对应的用户 ID
    String resetKey = PASSWORD_RESET_PREFIX + resetToken;
    Object userIdObj = redisTemplate.opsForValue().get(resetKey);

    if (userIdObj == null) {
      throw new BusinessException("重置 Token 已过期或无效");
    }

    Long userId = Long.parseLong(userIdObj.toString());

    // 获取用户
    User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

    // 更新密码
    String encodedPassword = passwordUtil.encode(newPassword);
    user.setPassword(encodedPassword);
    userRepository.save(user);

    // 删除重置 Token（使其失效）
    redisTemplate.delete(resetKey);

    log.info("用户 {} 密码重置成功", user.getUsername());
  }

  /** 生成随机重置 Token */
  private String generateResetToken() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }
}
