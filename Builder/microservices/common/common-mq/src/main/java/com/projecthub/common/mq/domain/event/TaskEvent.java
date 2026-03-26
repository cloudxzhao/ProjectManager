package com.projecthub.common.mq.domain.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 任务事件数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskEvent extends EventDataBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 项目负责人 ID
     */
    private Long assigneeId;

    /**
     * 项目负责人姓名
     */
    private String assigneeName;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 任务类型
     */
    private String taskType;

}
