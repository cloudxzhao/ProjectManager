package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建史诗请求")
public class CreateEpicRequest {
    @Schema(description = "史诗名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "优先级")
    private Integer priority;
}