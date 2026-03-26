package com.projecthub.common.mq.domain.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 项目事件数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectEvent extends EventDataBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目状态
     */
    private String status;

    /**
     * 创建人 ID
     */
    private Long ownerId;

    /**
     * 成员 ID 列表（逗号分隔）
     */
    private String memberIds;

}
