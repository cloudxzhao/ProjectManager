package com.projecthub.issue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("issues")
public class Issue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String issueKey;
    private String title;
    private String description;
    private String stepsToReproduce;
    private String environment;
    private Long projectId;
    private Long epicId;
    private Long taskId;
    private Long assigneeId;
    private String reporterId;
    private String status;
    private String priority;
    private String severity;
    private String type;
    private Integer storyPoints;
    private Long parentIssueId;
    private LocalDateTime resolvedAt;
    private LocalDateTime dueDate;
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}