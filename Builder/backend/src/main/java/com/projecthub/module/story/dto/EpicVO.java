package com.projecthub.module.story.dto;

import com.projecthub.common.constant.EpicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 史诗 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicVO {

  /** 史诗 ID */
  private Long id;

  /** 项目 ID */
  private Long projectId;

  /** 标题 */
  private String title;

  /** 描述 */
  private String description;

  /** 颜色 */
  private String color;

  /** 位置 */
  private Integer position;

  /** 状态：活跃/非活跃 */
  private EpicStatus status;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;

  /** 创建史诗请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    @NotBlank(message = "史诗标题不能为空")
    @Size(max = 200, message = "史诗标题最多 200 字符")
    private String title;

    /** 描述 */
    private String description;

    /** 颜色 */
    private String color;
  }

  /** 更新史诗请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    @Size(max = 200, message = "史诗标题最多 200 字符")
    private String title;

    /** 描述 */
    private String description;

    /** 颜色 */
    private String color;

    /** 状态：活跃/非活跃 */
    private EpicStatus status;
  }

  /** 切换史诗状态请求 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ToggleStatusRequest {
    @NotNull(message = "状态不能为空")
    private EpicStatus status;
  }
}
