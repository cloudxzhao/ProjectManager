package com.projecthub.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * 错误码规范:
 * - 1000-1999: 通用错误
 * - 2000-2999: 认证模块
 * - 3000-3999: 用户模块
 * - 4000-4999: 项目模块
 * - 5000-5999: 任务模块
 * - 6000-6999: 用户故事模块
 * - 7000-7999: 问题追踪模块
 * - 8000-8999: Wiki 模块
 * - 9000-9999: 报表模块
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误 1000-1999 ==========
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== 认证模块 2000-2999 ==========
    LOGIN_FAILED(2001, "登录失败"),
    TOKEN_EXPIRED(2002, "Token 已过期"),
    TOKEN_INVALID(2003, "Token 无效"),
    REGISTER_FAILED(2004, "注册失败"),
    LOGOUT_FAILED(2005, "登出失败"),

    // ========== 用户模块 3000-3999 ==========
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_ALREADY_EXISTS(3002, "用户已存在"),
    EMAIL_ALREADY_EXISTS(3003, "邮箱已被注册"),
    USERNAME_ALREADY_EXISTS(3004, "用户名已存在"),
    PASSWORD_ERROR(3005, "密码错误"),
    USER_INACTIVE(3006, "用户账号未激活"),
    USER_BANNED(3007, "用户账号已被封禁"),

    // ========== 项目模块 4000-4999 ==========
    PROJECT_NOT_FOUND(4001, "项目不存在"),
    PROJECT_PERMISSION_DENIED(4002, "无项目访问权限"),
    PROJECT_MEMBER_NOT_FOUND(4003, "项目成员不存在"),
    PROJECT_MEMBER_ALREADY_EXISTS(4004, "已是项目成员"),
    PROJECT_DATE_INVALID(4005, "项目日期无效"),

    // ========== 任务模块 5000-5999 ==========
    TASK_NOT_FOUND(5001, "任务不存在"),
    TASK_PERMISSION_DENIED(5002, "无任务访问权限"),
    TASK_INVALID_STATUS(5003, "任务状态无效"),
    TASK_MOVE_FAILED(5004, "任务移动失败"),

    // ========== 用户故事模块 6000-6999 ==========
    STORY_NOT_FOUND(6001, "用户故事不存在"),
    EPIC_NOT_FOUND(6002, "史诗不存在"),

    // ========== 问题追踪模块 7000-7999 ==========
    ISSUE_NOT_FOUND(7001, "问题不存在"),
    ISSUE_INVALID_TYPE(7002, "问题类型无效"),

    // ========== Wiki 模块 8000-8999 ==========
    WIKI_NOT_FOUND(8001, "Wiki 文档不存在"),
    WIKI_PERMISSION_DENIED(8002, "无 Wiki 访问权限"),

    // ========== 报表模块 9000-9999 ==========
    REPORT_NOT_FOUND(9001, "报表不存在");

    private final Integer code;
    private final String message;
}
