package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新任务请求
 */
@Data
@Schema(description = "更新任务请求")
public class UpdateTaskRequest {

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "故事点")
    private Integer storyPoints;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

}