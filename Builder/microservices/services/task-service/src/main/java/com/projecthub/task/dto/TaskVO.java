package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务 VO
 */
@Data
@Schema(description = "任务信息")
public class TaskVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务编号")
    private String taskKey;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人名称")
    private String assigneeName;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "故事点")
    private Integer storyPoints;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}