package com.projecthub.wiki.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建知识页面请求")
public class CreatePageRequest {
    @Schema(description = "空间ID")
    @NotNull(message = "空间ID不能为空")
    private Long spaceId;

    @Schema(description = "父页面ID")
    private Long parentId;

    @Schema(description = "页面标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "页面内容")
    private String content;

    @Schema(description = "URL别名")
    private String slug;

    @Schema(description = "排序号")
    private Integer orderNum;
}