package com.projecthub.module.wiki.dto;

import com.projecthub.module.wiki.enums.WikiStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wiki 搜索结果响应 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wiki 搜索结果")
public class WikiSearchResultVO {

  @Schema(description = "文档 ID")
  private Long id;

  @Schema(description = "项目 ID")
  private Long projectId;

  @Schema(description = "标题")
  private String title;

  @Schema(description = "摘要")
  private String summary;

  @Schema(description = "匹配的内容片段")
  private String highlight;

  @Schema(description = "状态")
  private WikiStatus status;

  @Schema(description = "作者 ID")
  private Long authorId;

  @Schema(description = "作者名称")
  private String authorName;

  @Schema(description = "浏览次数")
  private Integer viewCount;

  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  @Schema(description = "更新时间")
  private LocalDateTime updatedAt;
}
