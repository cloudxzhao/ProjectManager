package com.projecthub.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
public class JwtUtils {

    private JwtUtils() {
    }

    /**
     * 生成签名密钥
     */
    public static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     */
    public static String generateToken(String secret, String subject, Long userId, String username, String role, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(secret))
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String secret, String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("JWT token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String secret, String token) {
        try {
            Claims claims = parseToken(secret, token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("JWT token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public static Long getUserId(String secret, String token) {
        Claims claims = parseToken(secret, token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsername(String secret, String token) {
        Claims claims = parseToken(secret, token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 Token 中获取角色
     */
    public static String getRole(String secret, String token) {
        Claims claims = parseToken(secret, token);
        return claims != null ? (String) claims.get("role") : null;
    }

}