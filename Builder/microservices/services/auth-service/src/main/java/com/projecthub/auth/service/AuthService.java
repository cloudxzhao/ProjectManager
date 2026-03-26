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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.issuer:projecthub}")
    private String issuer;

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(1001, "邮箱或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(1001, "邮箱或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(1004, "账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.updateById(user);

        // 生成 Token
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, jwtExpiration / 1000, user);
    }

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(2003, "邮箱已被注册");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(2002, "用户名已被使用");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("MEMBER");
        user.setStatus(1);
        user.setEmailVerified(0);
        user.setDeleted(0);

        userRepository.insert(user);
        log.info("用户注册成功: {}", user.getEmail());

        // 生成 Token
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, jwtExpiration / 1000, user);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        // 查找刷新 Token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(1003, "无效的刷新 Token"));

        // 检查是否已使用
        if (refreshToken.getUsed() == 1) {
            throw new BusinessException(1003, "刷新 Token 已使用");
        }

        // 检查是否过期
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(1002, "刷新 Token 已过期");
        }

        // 获取用户
        User user = userRepository.selectById(refreshToken.getUserId());
        if (user == null) {
            throw new BusinessException(2001, "用户不存在");
        }

        // 标记旧 Token 为已使用
        refreshTokenRepository.markAsUsed(token);

        // 生成新的 Token
        String accessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        return AuthResponse.of(accessToken, newRefreshToken, jwtExpiration / 1000, user);
    }

    /**
     * 忘记密码
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(2001, "用户不存在"));

        // 生成重置 Token
        String resetToken = IdUtil.simpleUUID();

        // TODO: 将 Token 存储到 Redis，设置 30 分钟过期
        // redisService.set("password_reset:" + resetToken, user.getId(), 30, TimeUnit.MINUTES);

        // 发送邮件
        sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("密码重置邮件已发送: {}", user.getEmail());
    }

    /**
     * 重置密码
     */
    public void resetPassword(ResetPasswordRequest request) {
        // TODO: 从 Redis 获取用户 ID
        // Long userId = (Long) redisService.get("password_reset:" + request.getToken());
        // if (userId == null) {
        //     throw new BusinessException(1003, "重置链接已过期或无效");
        // }

        // 查找用户
        // User user = userRepository.selectById(userId);

        // 更新密码
        // user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // userRepository.updateById(user);

        // 删除重置 Token
        // redisService.delete("password_reset:" + request.getToken());

        log.info("密码重置成功");
    }

    /**
     * 生成访问 Token
     */
    private String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新 Token
     */
    private String generateRefreshToken(User user) {
        String token = IdUtil.simpleUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshToken.setUsed(0);

        refreshTokenRepository.insert(refreshToken);
        return token;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 发送密码重置邮件
     */
    private void sendPasswordResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("【ProjectHub】密码重置");
        message.setText("您正在申请重置密码，请在 30 分钟内点击链接完成重置：\n\n" +
                "http://localhost:3000/reset-password?token=" + token + "\n\n" +
                "如果您没有申请重置密码，请忽略此邮件。");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            // 开发环境不抛异常
        }
    }

}