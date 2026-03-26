package com.projecthub.common.api.enums;

import lombok.Getter;

/**
 * 项目状态枚举
 */
@Getter
public enum ProjectStatus {

    ACTIVE("ACTIVE", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String name;

    ProjectStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

}