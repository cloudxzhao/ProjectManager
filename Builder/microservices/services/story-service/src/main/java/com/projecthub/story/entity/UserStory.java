package com.projecthub.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_stories")
public class UserStory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String storyKey;
    private String title;
    private String description;
    private String acceptanceCriteria;
    private Long epicId;
    private Long projectId;
    private Long assigneeId;
    private String status;
    private String priority;
    private Integer storyPoints;
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}