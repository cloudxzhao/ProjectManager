package com.projecthub.issue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建问题请求")
public class CreateIssueRequest {
    @Schema(description = "问题标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "重现步骤")
    private String stepsToReproduce;

    @Schema(description = "环境信息")
    private String environment;

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "史诗ID")
    private Long epicId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "报告人ID")
    private String reporterId;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "严重程度")
    private String severity;

    @Schema(description = "问题类型")
    private String type;

    @Schema(description = "故事点数")
    private Integer storyPoints;

    @Schema(description = "父问题ID")
    private Long parentIssueId;

    @Schema(description = "截止日期")
    private String dueDate;
}