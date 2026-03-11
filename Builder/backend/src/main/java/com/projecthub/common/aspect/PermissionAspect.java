package com.projecthub.common.aspect;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.security.UserDetailsImpl;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** 权限校验切面 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

  private final PermissionService permissionService;

  /** 环绕通知 - 权限校验 */
  @Around("@annotation(com.projecthub.common.aspect.RequirePermission)")
  public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
    // 获取方法注解
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

    if (requirePermission == null) {
      return joinPoint.proceed();
    }

    // 获取当前用户
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserDetailsImpl userDetails)) {
      throw new BusinessException("用户未登录");
    }

    Long userId = userDetails.getId();
    String permissionCode = requirePermission.value();

    // 获取项目 ID (从参数中)
    Long projectId = extractProjectId(joinPoint, requirePermission.projectIdParam());

    // 校验权限
    if (projectId != null && !permissionService.hasPermission(userId, projectId, permissionCode)) {
      log.warn("权限不足：userId={}, projectId={}, permission={}", userId, projectId, permissionCode);
      throw new BusinessException(403, "权限不足");
    }

    // 管理员权限校验 (如果有项目 ID 则检查项目权限，否则检查全局权限)
    if (!permissionService.hasGlobalPermission(userId, permissionCode) && projectId == null) {
      log.warn("全局权限不足：userId={}, permission={}", userId, permissionCode);
      throw new BusinessException(403, "权限不足");
    }

    log.debug("权限校验通过：userId={}, permission={}", userId, permissionCode);

    return joinPoint.proceed();
  }

  /** 从方法参数中提取项目 ID */
  private Long extractProjectId(ProceedingJoinPoint joinPoint, String paramName) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    // 查找项目 ID 参数
    for (int i = 0; i < parameterNames.length; i++) {
      if (parameterNames[i].equals(paramName)) {
        if (args[i] instanceof Long) {
          return (Long) args[i];
        }
      }
    }

    return null;
  }
}
