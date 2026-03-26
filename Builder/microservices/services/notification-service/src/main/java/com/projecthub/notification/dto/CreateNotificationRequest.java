package com.projecthub.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建通知请求
 */
@Data
@Schema(description = "创建通知请求")
public class CreateNotificationRequest {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知渠道 (IN_APP/EMAIL)")
    private String channel;

    @Schema(description = "接收者邮箱")
    private String recipient;
}
