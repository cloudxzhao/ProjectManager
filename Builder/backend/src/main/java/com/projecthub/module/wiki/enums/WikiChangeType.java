package com.projecthub.module.wiki.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Wiki 变更类型枚举 */
@Getter
@AllArgsConstructor
@Schema(description = "Wiki 变更类型")
public enum WikiChangeType {
  @Schema(description = "创建")
  CREATE("CREATE", "创建"),

  @Schema(description = "更新")
  UPDATE("UPDATE", "更新"),

  @Schema(description = "恢复")
  RESTORE("RESTORE", "恢复");

  private final String code;
  private final String description;

  public static WikiChangeType fromCode(String code) {
    for (WikiChangeType type : values()) {
      if (type.getCode().equalsIgnoreCase(code)) {
        return type;
      }
    }
    return UPDATE;
  }
}
