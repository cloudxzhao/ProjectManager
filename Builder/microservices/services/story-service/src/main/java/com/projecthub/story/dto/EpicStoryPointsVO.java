package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Epic story points statistics DTO
 */
@Data
@Schema(description = "史诗故事点统计")
public class EpicStoryPointsVO {

    @Schema(description = "史诗ID")
    private Long epicId;

    @Schema(description = "史诗名称")
    private String epicName;

    @Schema(description = "总故事点")
    private Integer totalPoints;

    @Schema(description = "已完成故事点")
    private Integer completedPoints;

    @Schema(description = "完成率")
    private Double completionRate;
}