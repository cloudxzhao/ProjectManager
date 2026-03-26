package com.projecthub.common.mq.domain.event;

import lombok.Data;

import java.io.Serializable;

/**
 * 事件数据基类
 */
@Data
public abstract class EventDataBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件发生时间戳
     */
    private Long timestamp;

    /**
     * 操作人 ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

}
