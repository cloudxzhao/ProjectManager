package com.projecthub.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("epics")
public class Epic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private String status;
    private Integer priority;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}