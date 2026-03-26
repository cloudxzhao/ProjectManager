package com.projecthub.wiki.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Wiki 页面版本历史实体
 */
@Data
@TableName("wiki_page_history")
public class WikiPageHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 页面 ID
     */
    private Long pageId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 变更摘要
     */
    private String changeSummary;

    /**
     * 编辑者 ID
     */
    private Long editorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 是否当前版本
     */
    private Boolean isCurrent;
}
