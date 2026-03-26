package com.projecthub.wiki.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project info DTO for Feign client (read-only from project-service)
 */
@Data
@Schema(description = "项目信息 DTO")
public class ProjectInfoDTO {

    @Schema(description = "项目 ID")
    private Long id;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "项目图标")
    private String icon;

    @Schema(description = "项目主题色")
    private String color;

    @Schema(description = "负责人 ID")
    private Long ownerId;

    @Schema(description = "负责人名称")
    private String ownerName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "进度")
    private Integer progress;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
