package com.projecthub.module.wiki.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wiki 文档 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiVO {

  /** 文档 ID */
  private Long id;

  /** 项目 ID */
  private Long projectId;

  /** 父文档 ID */
  private Long parentId;

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** 位置 */
  private Integer position;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 子文档列表 */
  private List<WikiVO> children;

  /** 创建文档请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    /** 父文档 ID */
    private Long parentId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;
  }

  /** 更新文档请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    /** 标题 */
    private String title;

    /** 内容 */
    private String content;
  }
}
