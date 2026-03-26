package com.projecthub.wiki.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新 Wiki 页面请求（带版本历史）
 */
@Data
@Schema(description = "更新 Wiki 页面请求")
public class UpdatePageRequest {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "URL 别名")
    private String slug;

    @Schema(description = "排序号")
    private Integer orderNum;

    @Schema(description = "变更摘要")
    private String changeSummary;
}
