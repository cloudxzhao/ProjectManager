package com.projecthub.common.api.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatus {

    TODO("TODO", "待办"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    IN_REVIEW("IN_REVIEW", "审核中"),
    DONE("DONE", "已完成");

    private final String code;
    private final String name;

    TaskStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

}