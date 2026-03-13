package com.projecthub.common.exception;

import com.projecthub.common.constant.ErrorCode;
import lombok.Getter;

/** 业务异常类 */
@Getter
public class BusinessException extends RuntimeException {

  /** HTTP 状态码 */
  private final Integer httpStatus;

  /** 错误码 */
  private final Integer code;

  /** 使用错误码枚举构造 */
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.httpStatus = errorCode.getHttpStatus();
    this.code = errorCode.getCode();
  }

  /** 使用错误码和自定义消息构造 */
  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.httpStatus = errorCode.getHttpStatus();
    this.code = errorCode.getCode();
  }

  /** 使用自定义错误码和消息构造 */
  public BusinessException(Integer code, String message) {
    super(message);
    this.httpStatus = 200; // 默认返回 200，错误码在 body 中
    this.code = code;
  }

  /** 使用自定义 HTTP 状态码、错误码和消息构造 */
  public BusinessException(Integer httpStatus, Integer code, String message) {
    super(message);
    this.httpStatus = httpStatus;
    this.code = code;
  }

  /** 使用自定义消息构造 (默认错误码 500) */
  public BusinessException(String message) {
    super(message);
    this.httpStatus = 200;
    this.code = 500;
  }
}
