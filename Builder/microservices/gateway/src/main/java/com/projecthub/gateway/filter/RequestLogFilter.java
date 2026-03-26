package com.projecthub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求日志全局过滤器
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 生成或获取请求 ID
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        // 记录开始时间
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        String finalRequestId = requestId;

        // 添加请求 ID 到请求头
        exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        // 记录请求信息
        logRequest(exchange, finalRequestId);

        // 执行过滤器链
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 记录响应信息
            logResponse(exchange, finalRequestId);
        }));
    }

    /**
     * 记录请求信息
     */
    private void logRequest(ServerWebExchange exchange, String requestId) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        log.info("[{}] >>> {} {} from {} user={}", requestId, method, path, clientIp, userId);
    }

    /**
     * 记录响应信息
     */
    private void logResponse(ServerWebExchange exchange, String requestId) {
        Long startTime = exchange.getAttribute(START_TIME_ATTR);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        int statusCode = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : 0;

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        log.info("[{}] <<< {} {} - {} ({}ms)", requestId, method, path, statusCode, duration);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        // 处理多 IP 的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -200;  // 最先执行，记录完整请求
    }

}