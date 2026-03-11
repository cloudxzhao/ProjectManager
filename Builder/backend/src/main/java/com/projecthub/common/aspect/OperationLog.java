package com.projecthub.common.aspect;

import java.lang.annotation.*;

/** 操作日志注解 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

  /** 操作描述 */
  String value() default "";

  /** 模块名称 */
  String module() default "";

  /** 操作类型 */
  String operationType() default "";
}
