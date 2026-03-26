package com.projecthub.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务实体
 */
@Data
@TableName("tasks")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务编号（如 TASK-001）
     */
    private String taskKey;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 负责人ID
     */
    private Long assigneeId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 状态：TODO, IN_PROGRESS, IN_REVIEW, DONE
     */
    private String status;

    /**
     * 优先级：LOW, MEDIUM, HIGH, URGENT
     */
    private String priority;

    /**
     * 故事点
     */
    private Integer storyPoints;

    /**
     * 截止日期
     */
    private LocalDate dueDate;

    /**
     * 看板列ID
     */
    private Long columnId;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

}