package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 VO
 */
@Data
@Schema(description = "评论信息")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "评论者ID")
    private Long userId;

    @Schema(description = "评论者名称")
    private String username;

    @Schema(description = "评论者头像")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}