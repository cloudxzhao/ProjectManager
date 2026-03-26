package com.projecthub.common.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Feign 降级工厂基类
 * 提供通用的降级处理逻辑
 *
 * @param <T> Feign Client 接口类型
 */
@Slf4j
@Component
public class DefaultFallbackFactory<T> implements FallbackFactory<T> {

    private final String serviceName;

    public DefaultFallbackFactory(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public T create(Throwable cause) {
        log.error("{} 服务调用降级: {}", serviceName, cause.getMessage(), cause);
        return createFallback(cause);
    }

    /**
     * 创建降级实现，由子类或具体服务覆盖
     */
    @SuppressWarnings("unchecked")
    protected T createFallback(Throwable cause) {
        // 返回一个动态代理对象，所有方法都返回空或默认值
        return (T) new Object() {
            public String toString() {
                return "Fallback for " + serviceName + ": " + cause.getMessage();
            }
        };
    }

}