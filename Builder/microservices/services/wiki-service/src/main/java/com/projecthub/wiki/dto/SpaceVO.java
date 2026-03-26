package com.projecthub.wiki.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "知识空间视图对象")
public class SpaceVO {
    @Schema(description = "空间ID")
    private Long id;

    @Schema(description = "空间名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "所有者ID")
    private String ownerId;

    @Schema(description = "页面数量")
    private Integer pageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}