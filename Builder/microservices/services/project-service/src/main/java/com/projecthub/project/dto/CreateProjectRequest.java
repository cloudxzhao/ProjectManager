package com.projecthub.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建项目请求
 */
@Data
@Schema(description = "创建项目请求")
public class CreateProjectRequest {

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "项目图标")
    private String icon;

    @Schema(description = "项目主题色")
    private String color;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "项目负责人ID")
    private Long ownerId;

    @Schema(description = "初始成员ID列表")
    private List<Long> memberIds;

}