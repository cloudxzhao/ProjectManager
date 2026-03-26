package com.projecthub.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体
 */
@Data
@TableName("projects")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目图标
     */
    private String icon;

    /**
     * 项目主题色
     */
    private String color;

    /**
     * 项目负责人ID
     */
    private Long ownerId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态：ACTIVE-进行中, COMPLETED-已完成, ARCHIVED-已归档
     */
    private String status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 创建者ID
     */
    private Long creatorId;

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