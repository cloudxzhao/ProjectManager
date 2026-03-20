package com.projecthub.module.wiki.dto;

import com.projecthub.module.wiki.util.WikiContentUtils.DiffLine;
import com.projecthub.module.wiki.util.WikiContentUtils.DiffType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 版本对比响应 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "版本对比结果")
public class VersionDiffVO {

  @Schema(description = "文档 ID")
  private Long documentId;

  @Schema(description = "文档标题")
  private String title;

  @Schema(description = "原始版本号")
  private Integer oldVersion;

  @Schema(description = "目标版本号")
  private Integer newVersion;

  @Schema(description = "原始内容")
  private String oldContent;

  @Schema(description = "目标内容")
  private String newContent;

  @Schema(description = "差异行列表")
  private List<DiffLine> diffLines;

  @Schema(description = "HTML 格式的差异")
  private String diffHtml;

  @Schema(description = "新增行数")
  private int addedLines;

  @Schema(description = "删除行数")
  private int removedLines;

  @Schema(description = "修改行数")
  private int changedLines;

  /** 计算统计信息 */
  public static VersionDiffVO calculateStats(VersionDiffVO vo) {
    if (vo.getDiffLines() != null) {
      int added = 0;
      int removed = 0;
      int changed = 0;

      for (DiffLine line : vo.getDiffLines()) {
        if (line.getType() == DiffType.ADDED) {
          added++;
        } else if (line.getType() == DiffType.REMOVED) {
          removed++;
        }
      }

      vo.setAddedLines(added);
      vo.setRemovedLines(removed);
      vo.setChangedLines(changed);
    }
    return vo;
  }
}
