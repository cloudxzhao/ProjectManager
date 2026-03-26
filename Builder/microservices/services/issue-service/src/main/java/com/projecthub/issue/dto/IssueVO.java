package com.projecthub.issue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "问题视图对象")
public class IssueVO {
    @Schema(description = "问题ID")
    private Long id;

    @Schema(description = "问题Key")
    private String issueKey;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "重现步骤")
    private String stepsToReproduce;

    @Schema(description = "环境信息")
    private String environment;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "史诗ID")
    private Long epicId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人名称")
    private String assigneeName;

    @Schema(description = "报告人ID")
    private String reporterId;

    @Schema(description = "状态")
    private String status;

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

    @Schema(description = "解决时间")
    private LocalDateTime resolvedAt;

    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "子问题数量")
    private Integer subIssueCount;
}