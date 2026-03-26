package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 移动任务请求
 */
@Data
@Schema(description = "移动任务请求")
public class MoveTaskRequest {

    @Schema(description = "目标状态")
    @NotBlank(message = "目标状态不能为空")
    private String status;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "目标列ID")
    private Long columnId;

}