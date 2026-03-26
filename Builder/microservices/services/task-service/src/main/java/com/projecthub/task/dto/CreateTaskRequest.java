package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建任务请求
 */
@Data
@Schema(description = "创建任务请求")
public class CreateTaskRequest {

    @Schema(description = "任务标题")
    @NotBlank(message = "任务标题不能为空")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "优先级")
    private String priority = "MEDIUM";

    @Schema(description = "故事点")
    private Integer storyPoints;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

}