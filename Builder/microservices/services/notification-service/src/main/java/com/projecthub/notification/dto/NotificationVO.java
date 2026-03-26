package com.projecthub.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Notification VO
 */
@Data
@Schema(description = "通知 VO")
public class NotificationVO {

    @Schema(description = "通知 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知渠道")
    private String channel;

    @Schema(description = "接收者")
    private String recipient;

    @Schema(description = "状态 (UNREAD/READ)")
    private String status;

    @Schema(description = "发送时间")
    private LocalDateTime sentAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
