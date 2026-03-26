package com.projecthub.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Sentinel 限流熔断配置
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 限流回调处理器
     */
    @PostConstruct
    public void initBlockHandlers() {
        BlockRequestHandler blockRequestHandler = (exchange, t) ->
            ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                    "code", 429,
                    "message", "请求过于频繁，请稍后再试",
                    "data", null
                ));
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
        log.info("Sentinel 限流回调处理器初始化完成");
    }

}
