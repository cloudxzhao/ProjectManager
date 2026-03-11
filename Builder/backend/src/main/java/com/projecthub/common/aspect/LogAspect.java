package com.projecthub.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthub.module.log.repository.OperationLogRepository;
import com.projecthub.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 操作日志切面 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

  private final OperationLogRepository operationLogRepository;
  private final ObjectMapper objectMapper;

  /** 切入点：所有带 @OperationLog 注解的方法 */
  @Pointcut("@annotation(opLog)")
  public void logPointcut() {}

  /** 环绕通知 */
  @Around("@annotation(opLog)")
  public Object around(ProceedingJoinPoint pjp, OperationLog opLog) throws Throwable {
    long startTime = System.currentTimeMillis();

    // 获取请求信息
    HttpServletRequest request = getRequest();
    String ipAddress = request != null ? getClientIp(request) : "unknown";
    String method = request != null ? request.getMethod() : "unknown";

    // 获取用户信息
    Long userId = getCurrentUserId();
    String username = getCurrentUsername();

    // 获取请求参数
    String params = "";
    try {
      Object[] args = pjp.getArgs();
      params = objectMapper.writeValueAsString(args);
    } catch (Exception e) {
      log.warn("序列化请求参数失败", e);
    }

    // 执行方法
    Object result = null;
    String resultStr = "";
    try {
      result = pjp.proceed();
      resultStr = objectMapper.writeValueAsString(result);
    } catch (Throwable e) {
      resultStr = "Error: " + e.getMessage();
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;

      // 保存操作日志
      saveOperationLog(
          userId,
          username,
          opLog.module(),
          opLog.operationType(),
          method,
          params,
          resultStr,
          ipAddress,
          duration);
    }

    return result;
  }

  /** 保存操作日志 */
  private void saveOperationLog(
      Long userId,
      String username,
      String module,
      String operation,
      String method,
      String params,
      String result,
      String ipAddress,
      Long duration) {
    try {
      com.projecthub.module.log.entity.OperationLog operationLogEntity =
          com.projecthub.module.log.entity.OperationLog.builder()
              .userId(userId)
              .username(username)
              .module(module)
              .operation(operation)
              .method(method)
              .params(params)
              .result(result)
              .ipAddress(ipAddress)
              .duration(duration)
              .build();

      operationLogRepository.save(operationLogEntity);
      log.info("保存操作日志：module={}, operation={}, duration={}ms", module, operation, duration);
    } catch (Exception e) {
      log.error("保存操作日志失败", e);
    }
  }

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof UserDetailsImpl) {
        return ((UserDetailsImpl) principal).getId();
      }
    } catch (Exception ignored) {
    }
    return 0L;
  }

  /** 获取当前用户名 */
  private String getCurrentUsername() {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof UserDetailsImpl) {
        return ((UserDetailsImpl) principal).getUsername();
      }
    } catch (Exception ignored) {
    }
    return "anonymous";
  }

  /** 获取 HttpServletRequest */
  private HttpServletRequest getRequest() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attributes != null ? attributes.getRequest() : null;
    } catch (Exception ignored) {
      return null;
    }
  }

  /** 获取客户端 IP 地址 */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 多个代理时取第一个
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}
