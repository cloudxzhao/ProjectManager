package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建用户故事请求")
public class CreateStoryRequest {
    @Schema(description = "故事标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "验收标准")
    private String acceptanceCriteria;

    @Schema(description = "史诗ID")
    private Long epicId;

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "故事点数")
    private Integer storyPoints;
}