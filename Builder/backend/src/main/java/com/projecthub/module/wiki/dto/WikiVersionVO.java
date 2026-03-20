package com.projecthub.module.wiki.dto;

import com.projecthub.module.wiki.enums.WikiChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wiki 版本信息响应 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wiki 版本信息")
public class WikiVersionVO {

  @Schema(description = "版本 ID")
  private Long id;

  @Schema(description = "文档 ID")
  private Long documentId;

  @Schema(description = "版本号")
  private Integer version;

  @Schema(description = "变更内容")
  private String content;

  @Schema(description = "HTML 内容")
  private String contentHtml;

  @Schema(description = "变更日志")
  private String changeLog;

  @Schema(description = "变更类型")
  private WikiChangeType changeType;

  @Schema(description = "修改人 ID")
  private Long userId;

  @Schema(description = "修改人名称")
  private String userName;

  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  /** 恢复版本请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RestoreRequest {
    @Schema(description = "变更日志")
    private String changeLog;
  }
}
