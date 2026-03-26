package com.projecthub.gateway.filter;

import com.projecthub.gateway.config.JwtTokenProvider;
import com.projecthub.gateway.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT 认证全局过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();

        // 检查是否是忽略认证的路径
        if (isIgnoredPath(requestPath)) {
            log.debug("路径 {} 无需认证，放行", requestPath);
            return chain.filter(exchange);
        }

        // 获取 Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader)) {
            log.warn("请求路径 {} 缺少 Authorization Header", requestPath);
            return unauthorized(exchange, "缺少认证信息");
        }

        // 验证 Bearer 格式
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("请求路径 {} Authorization Header 格式错误", requestPath);
            return unauthorized(exchange, "认证格式错误");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 验证 Token
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("请求路径 {} Token 无效或已过期", requestPath);
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 解析用户信息并注入到请求头
        Long userId = jwtTokenProvider.getUserId(token);
        String username = jwtTokenProvider.getUsername(token);
        String role = jwtTokenProvider.getRole(token);

        if (userId == null) {
            log.warn("请求路径 {} Token 中缺少用户信息", requestPath);
            return unauthorized(exchange, "Token 缺少用户信息");
        }

        // 构建新的请求，注入用户信息
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Name", username != null ? username : "")
                .header("X-User-Role", role != null ? role : "")
                .build();

        log.debug("用户 {} (ID: {}) 访问 {}", username, userId, requestPath);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 检查是否是忽略认证的路径
     */
    private boolean isIgnoredPath(String path) {
        if (securityProperties.getIgnore() == null) {
            return false;
        }
        return securityProperties.getIgnore().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;  // 优先级高，先执行认证
    }

}