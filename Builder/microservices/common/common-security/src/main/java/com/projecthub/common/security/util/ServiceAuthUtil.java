package com.projecthub.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务间调用鉴权工具
 *
 * 用于生成和验证服务间调用的 JWT Token
 */
@Slf4j
@Component
public class ServiceAuthUtil {

    @Value("${service.auth.secret:ProjectHubServiceSecretKey2024}")
    private String secretKey;

    @Value("${service.auth.expiration:3600000}")
    private long expiration; // 默认 1 小时

    /**
     * 生成服务间调用 Token
     *
     * @param sourceService 调用方服务名
     * @param targetService 被调用方服务名
     * @return JWT Token
     */
    public String generateServiceToken(String sourceService, String targetService) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "service-to-service");
        claims.put("source", sourceService);
        claims.put("target", targetService);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(sourceService + "->" + targetService)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 验证服务 Token
     *
     * @param token JWT Token
     * @return 验证结果
     */
    public boolean validateServiceToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            return "service-to-service".equals(type);

        } catch (Exception e) {
            log.error("验证服务 Token 失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取调用方服务名
     *
     * @param token JWT Token
     * @return 调用方服务名
     */
    public String getSourceService(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("source", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取被调用方服务名
     *
     * @param token JWT Token
     * @return 被调用方服务名
     */
    public String getTargetService(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("target", String.class);
        } catch (Exception e) {
            return null;
        }
    }

}
