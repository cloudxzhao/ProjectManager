package com.projecthub.security;

import com.projecthub.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWT 认证过滤器 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final RedisTemplate<String, Object> redisTemplate;

  /** Token 前缀 */
  private static final String BEARER_PREFIX = "Bearer ";

  /** Token 黑名单前缀 */
  private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // 从请求头中获取 Token
      String token = extractTokenFromRequest(request);

      if (StringUtils.hasText(token)) {
        // 检查 Token 是否在黑名单中
        if (isTokenBlacklisted(token)) {
          log.debug("Token 在黑名单中，拒绝请求");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/json;charset=UTF-8");
          response.getWriter().write("{\"code\":401,\"message\":\"Token 已失效\"}");
          return;
        }

        String username = jwtUtil.getUsernameFromToken(token);

        if (StringUtils.hasText(username)
            && SecurityContextHolder.getContext().getAuthentication() == null) {

          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          if (jwtUtil.validateToken(token, username)) {
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          }
        }
      }
    } catch (Exception e) {
      log.error("无法设置用户认证信息：{}", e.getMessage(), e);
    }

    filterChain.doFilter(request, response);
  }

  /** 检查 Token 是否在黑名单中 */
  private boolean isTokenBlacklisted(String token) {
    String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
  }

  /** 从请求中提取 Token */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }

    // 也支持从 query parameter 中获取 token
    String token = request.getParameter("token");
    if (StringUtils.hasText(token)) {
      return token;
    }

    return null;
  }
}
