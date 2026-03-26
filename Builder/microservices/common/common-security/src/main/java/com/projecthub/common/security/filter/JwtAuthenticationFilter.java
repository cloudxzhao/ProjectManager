package com.projecthub.common.security.filter;

import com.projecthub.common.security.domain.LoginUser;
import com.projecthub.common.security.util.JwtUtils;
import com.projecthub.common.security.util.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中解析用户信息并设置到上下文
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:projecthub-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 从请求头获取用户信息（由 Gateway 注入）
            String userIdStr = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            String role = request.getHeader("X-User-Role");

            if (StringUtils.hasText(userIdStr)) {
                LoginUser loginUser = new LoginUser();
                loginUser.setUserId(Long.parseLong(userIdStr));
                loginUser.setUsername(username);
                loginUser.setRole(role);
                UserContextHolder.setUser(loginUser);
                log.debug("用户上下文设置成功: userId={}, username={}", userIdStr, username);
            }
        } catch (Exception e) {
            log.error("解析用户信息失败", e);
        }

        filterChain.doFilter(request, response);
    }

}