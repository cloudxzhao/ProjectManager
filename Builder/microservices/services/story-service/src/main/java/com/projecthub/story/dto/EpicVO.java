package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "史诗视图对象")
public class EpicVO {
    @Schema(description = "史诗ID")
    private Long id;

    @Schema(description = "史诗名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "开始日期")
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    private LocalDateTime endDate;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "包含的故事数")
    private Integer storyCount;
}