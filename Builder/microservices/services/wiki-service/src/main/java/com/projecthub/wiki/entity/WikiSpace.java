package com.projecthub.wiki.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wiki_spaces")
public class WikiSpace {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private String icon;
    private String ownerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}