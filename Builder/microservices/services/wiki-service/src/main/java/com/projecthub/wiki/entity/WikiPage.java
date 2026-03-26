package com.projecthub.wiki.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wiki_pages")
public class WikiPage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long parentId;
    private String title;
    private String content;
    private String slug;
    private Integer orderNum;
    private Integer level;
    private Long creatorId;
    private Long lastEditorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}