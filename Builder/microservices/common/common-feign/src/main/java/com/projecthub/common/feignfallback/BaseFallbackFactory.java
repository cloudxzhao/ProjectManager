package com.projecthub.common.feignfallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * Feign 客户端熔断降级基类
 *
 * @param <T> Feign 客户端类型
 */
@Slf4j
public abstract class BaseFallbackFactory<T> implements FallbackFactory<T> {

    /**
     * 获取降级时的默认返回值
     *
     * @param cause 触发降级的异常
     * @return 降级返回值
     */
    protected abstract T getFallbackInstance(Throwable cause);

    @Override
    public T create(Throwable cause) {
        log.error("Feign 调用触发熔断降级", cause);
        return getFallbackInstance(cause);
    }

}
