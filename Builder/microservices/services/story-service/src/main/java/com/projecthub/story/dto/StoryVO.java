package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户故事视图对象")
public class StoryVO {
    @Schema(description = "故事ID")
    private Long id;

    @Schema(description = "故事Key")
    private String storyKey;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "验收标准")
    private String acceptanceCriteria;

    @Schema(description = "史诗ID")
    private Long epicId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人名称")
    private String assigneeName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "故事点数")
    private Integer storyPoints;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}