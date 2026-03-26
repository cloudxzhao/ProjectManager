package com.projecthub.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新 Token 实体
 */
@Data
@TableName("refresh_tokens")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * Token 值
     */
    private String token;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 是否已使用
     */
    private Integer used;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}