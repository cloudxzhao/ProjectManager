package com.projecthub.issue.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Issue 创建事件数据
 */
@Data
@Schema(description = "问题创建事件数据")
public class IssueCreatedEventData {

    @Schema(description = "问题 ID")
    private Long issueId;

    @Schema(description = "问题 Key")
    private String issueKey;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题类型")
    private String type;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "严重程度")
    private String severity;

    @Schema(description = "项目 ID")
    private Long projectId;

    @Schema(description = "负责人 ID")
    private Long assigneeId;

    @Schema(description = "报告人 ID")
    private String reporterId;

    @Schema(description = "创建人 ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
