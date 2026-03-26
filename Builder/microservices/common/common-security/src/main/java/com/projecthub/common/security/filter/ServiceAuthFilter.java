package com.projecthub.common.security.filter;

import com.projecthub.common.security.util.ServiceAuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 服务间调用认证过滤器
 *
 * 验证来自其他服务的请求是否携带有效的 Service Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAuthFilter extends OncePerRequestFilter {

    private final ServiceAuthUtil serviceAuthUtil;

    /**
     * 不需要服务间认证的路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/actuator/health",
            "/actuator/info",
            "/swagger-resources",
            "/v2/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 排除不需要认证的路径
        if (EXCLUDE_PATHS.stream().anyMatch(requestURI::contains)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取服务间调用 Token
        String serviceToken = request.getHeader("X-Service-Token");

        if (serviceToken != null && !serviceToken.isEmpty()) {
            // 验证服务 Token
            if (!serviceAuthUtil.validateServiceToken(serviceToken)) {
                log.warn("服务间调用 Token 验证失败：URI={}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Invalid service token\"}");
                return;
            }

            // 记录调用方服务
            String sourceService = serviceAuthUtil.getSourceService(serviceToken);
            log.debug("服务间调用验证通过：source={}, URI={}", sourceService, requestURI);

            // 将调用方服务名放入请求属性
            request.setAttribute("sourceService", sourceService);
        }

        filterChain.doFilter(request, response);
    }

}
