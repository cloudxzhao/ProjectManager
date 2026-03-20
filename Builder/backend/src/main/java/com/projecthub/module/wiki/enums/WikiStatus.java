package com.projecthub.module.wiki.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Wiki 文档状态枚举 */
@Getter
@AllArgsConstructor
@Schema(description = "Wiki 文档状态")
public enum WikiStatus {
  @Schema(description = "草稿")
  DRAFT("DRAFT", "草稿"),

  @Schema(description = "已发布")
  PUBLISHED("PUBLISHED", "已发布"),

  @Schema(description = "已归档")
  ARCHIVED("ARCHIVED", "已归档");

  private final String code;
  private final String description;

  public static WikiStatus fromCode(String code) {
    for (WikiStatus status : values()) {
      if (status.getCode().equalsIgnoreCase(code)) {
        return status;
      }
    }
    return PUBLISHED;
  }
}
