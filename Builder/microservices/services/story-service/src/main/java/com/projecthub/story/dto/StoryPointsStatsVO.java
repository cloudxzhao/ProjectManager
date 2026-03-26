package com.projecthub.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Story points statistics DTO
 */
@Data
@Schema(description = "故事点统计")
public class StoryPointsStatsVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "总故事点")
    private Integer totalPoints;

    @Schema(description = "已完成故事点")
    private Integer completedPoints;

    @Schema(description = "进行中故事点")
    private Integer inProgressPoints;

    @Schema(description = "未开始故事点")
    private Integer notStartedPoints;

    @Schema(description = "完成率")
    private Double completionRate;

    @Schema(description = "史诗统计列表")
    private List<EpicStoryPointsVO> epicStats;
}