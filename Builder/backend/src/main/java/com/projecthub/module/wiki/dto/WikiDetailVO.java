package com.projecthub.module.wiki.dto;

import com.projecthub.module.wiki.enums.WikiStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wiki 文档详情响应 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wiki 文档详情")
public class WikiDetailVO {

  @Schema(description = "文档 ID")
  private Long id;

  @Schema(description = "项目 ID")
  private Long projectId;

  @Schema(description = "父文档 ID")
  private Long parentId;

  @Schema(description = "标题")
  private String title;

  @Schema(description = "Markdown 内容")
  private String content;

  @Schema(description = "HTML 内容")
  private String contentHtml;

  @Schema(description = "摘要")
  private String summary;

  @Schema(description = "作者 ID")
  private Long authorId;

  @Schema(description = "作者名称")
  private String authorName;

  @Schema(description = "版本号")
  private Integer version;

  @Schema(description = "排序号")
  private Integer orderNum;

  @Schema(description = "状态")
  private WikiStatus status;

  @Schema(description = "父路径")
  private String parentPath;

  @Schema(description = "层级")
  private Integer level;

  @Schema(description = "浏览次数")
  private Integer viewCount;

  @Schema(description = "是否有子文档")
  private Boolean hasChildren;

  @Schema(description = "子文档列表")
  private List<WikiDetailVO> children;

  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  @Schema(description = "更新时间")
  private LocalDateTime updatedAt;
}
