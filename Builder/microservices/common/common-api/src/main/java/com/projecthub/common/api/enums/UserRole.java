package com.projecthub.common.api.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    ADMIN("ADMIN", "系统管理员"),
    ENTERPRISE_ADMIN("ENTERPRISE_ADMIN", "企业管理员"),
    PROJECT_MANAGER("PROJECT_MANAGER", "项目经理"),
    MEMBER("MEMBER", "团队成员"),
    GUEST("GUEST", "访客");

    private final String code;
    private final String name;

    UserRole(String code, String name) {
        this.code = code;
        this.name = name;
    }

}