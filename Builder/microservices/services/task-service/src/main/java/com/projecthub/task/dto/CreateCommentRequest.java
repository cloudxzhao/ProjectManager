package com.projecthub.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建评论请求
 */
@Data
@Schema(description = "创建评论请求")
public class CreateCommentRequest {

    @Schema(description = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @Schema(description = "父评论ID（回复时使用）")
    private Long parentId;

}