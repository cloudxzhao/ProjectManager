package com.projecthub.common.api.enums;

import lombok.Getter;

/**
 * 任务优先级枚举
 */
@Getter
public enum TaskPriority {

    LOW("LOW", "低", "#52c41a"),
    MEDIUM("MEDIUM", "中", "#faad14"),
    HIGH("HIGH", "高", "#fa8c16"),
    URGENT("URGENT", "紧急", "#f5222d");

    private final String code;
    private final String name;
    private final String color;

    TaskPriority(String code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }

}