package com.projecthub.wiki.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Wiki 页面版本历史 VO
 */
@Data
@Schema(description = "Wiki 页面版本历史视图对象")
public class PageHistoryVO {

    @Schema(description = "历史记录 ID")
    private Long id;

    @Schema(description = "页面 ID")
    private Long pageId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "编辑者 ID")
    private Long editorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否当前版本")
    private Boolean isCurrent;
}
