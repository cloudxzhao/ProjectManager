package com.projecthub.wiki.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "知识页面视图对象")
public class PageVO {
    @Schema(description = "页面ID")
    private Long id;

    @Schema(description = "空间ID")
    private Long spaceId;

    @Schema(description = "父页面ID")
    private Long parentId;

    @Schema(description = "页面标题")
    private String title;

    @Schema(description = "页面内容")
    private String content;

    @Schema(description = "URL别名")
    private String slug;

    @Schema(description = "排序号")
    private Integer orderNum;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "最后编辑人ID")
    private Long lastEditorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "子页面")
    private List<PageVO> children;
}