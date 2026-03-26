package com.projecthub.common.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Feign 请求拦截器 - 传递请求头
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final String[] HEADERS_TO_PROPAGATE = {
            "X-Request-Id",
            "X-User-Id",
            "X-User-Name",
            "X-User-Role",
            "Authorization"
    };

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        if (request == null) {
            return;
        }

        // 传递指定的请求头
        for (String headerName : HEADERS_TO_PROPAGATE) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                template.header(headerName, headerValue);
                log.debug("Feign 传递 Header: {} = {}", headerName, headerValue);
            }
        }

        // 如果没有 X-Request-Id，生成一个
        if (request.getHeader("X-Request-Id") == null) {
            template.header("X-Request-Id", java.util.UUID.randomUUID().toString().replace("-", ""));
        }
    }

}