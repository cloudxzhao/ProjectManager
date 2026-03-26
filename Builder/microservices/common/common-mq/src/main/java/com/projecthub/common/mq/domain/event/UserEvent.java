package com.projecthub.common.mq.domain.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户事件数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends EventDataBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色
     */
    private String role;

    /**
     * 操作人 ID（如果是管理员操作）
     */
    private Long operatorId;

}
