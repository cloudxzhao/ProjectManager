package com.projecthub.module.auth.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.JwtUtil;
import com.projecthub.common.util.PasswordUtil;
import com.projecthub.module.auth.dto.AuthResponse;
import com.projecthub.module.auth.dto.LoginRequest;
import com.projecthub.module.auth.dto.RegisterRequest;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Token 黑名单前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            // 获取用户信息
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 生成 Token
            String accessToken = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getId());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername(), userDetails.getId());

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

    /**
     * 用户注册
     */
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
        User user = User.builder()
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

    /**
     * 刷新 Token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新 Token
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            // 检查 Token 是否在黑名单中
            if (isTokenBlacklisted(refreshToken)) {
                throw new BusinessException("Token 已失效");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 生成新的 Token
            String newAccessToken = jwtUtil.generateToken(username, user.getId());
            String newRefreshToken = jwtUtil.generateRefreshToken(username, user.getId());

            log.info("Token 刷新成功：{}", username);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationDate(newAccessToken).getTime() - System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("刷新 Token 失败：{}", e.getMessage());
            throw new BusinessException("刷新 Token 失败");
        }
    }

    /**
     * 用户登出
     */
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

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }
}
