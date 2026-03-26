package com.projecthub.common.api.constant;

/**
 * 错误码常量
 */
public interface ErrorCode {

    // ========== 通用错误码 1-999 ==========
    int SUCCESS = 200;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int METHOD_NOT_ALLOWED = 405;
    int INTERNAL_SERVER_ERROR = 500;
    int SERVICE_UNAVAILABLE = 503;

    // ========== 认证相关 1000-1999 ==========
    int AUTH_LOGIN_FAILED = 1001;
    int AUTH_TOKEN_EXPIRED = 1002;
    int AUTH_TOKEN_INVALID = 1003;
    int AUTH_ACCOUNT_DISABLED = 1004;
    int AUTH_ACCOUNT_LOCKED = 1005;
    int AUTH_PASSWORD_WRONG = 1006;
    int AUTH_CAPTCHA_WRONG = 1007;

    // ========== 用户相关 2000-2999 ==========
    int USER_NOT_FOUND = 2001;
    int USER_ALREADY_EXISTS = 2002;
    int USER_EMAIL_EXISTS = 2003;
    int USER_PHONE_EXISTS = 2004;
    int USER_PASSWORD_NOT_MATCH = 2005;

    // ========== 项目相关 3000-3999 ==========
    int PROJECT_NOT_FOUND = 3001;
    int PROJECT_NAME_EXISTS = 3002;
    int PROJECT_NO_PERMISSION = 3003;
    int PROJECT_MEMBER_NOT_FOUND = 3004;
    int PROJECT_MEMBER_ALREADY_EXISTS = 3005;

    // ========== 任务相关 4000-4999 ==========
    int TASK_NOT_FOUND = 4001;
    int TASK_NO_PERMISSION = 4002;
    int TASK_STATUS_INVALID = 4003;
    int TASK_ASSIGNEE_NOT_MEMBER = 4004;

    // ========== 服务间调用 9000-9999 ==========
    int SERVICE_CALL_FAILED = 9001;
    int SERVICE_TIMEOUT = 9002;
    int SERVICE_CIRCUIT_BREAKER = 9003;

}