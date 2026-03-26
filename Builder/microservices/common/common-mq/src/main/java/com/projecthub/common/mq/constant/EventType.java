package com.projecthub.common.mq.constant;

/**
 * 事件类型常量
 */
public interface EventType {

    // ========== 用户相关事件 ==========
    String USER_CREATED = "user.created";
    String USER_UPDATED = "user.updated";
    String USER_DELETED = "user.deleted";

    // ========== 项目相关事件 ==========
    String PROJECT_CREATED = "project.created";
    String PROJECT_UPDATED = "project.updated";
    String PROJECT_DELETED = "project.deleted";
    String PROJECT_MEMBER_ADDED = "project.member.added";
    String PROJECT_MEMBER_REMOVED = "project.member.removed";

    // ========== 任务相关事件 ==========
    String TASK_CREATED = "task.created";
    String TASK_UPDATED = "task.updated";
    String TASK_DELETED = "task.deleted";
    String TASK_ASSIGNED = "task.assigned";
    String TASK_STATUS_CHANGED = "task.status.changed";

    // ========== 评论相关事件 ==========
    String COMMENT_CREATED = "comment.created";

    // ========== Wiki 相关事件 ==========
    String WIKI_CREATED = "wiki.created";
    String WIKI_UPDATED = "wiki.updated";

    // ========== Issue 相关事件 ==========
    String ISSUE_CREATED = "issue.created";
    String ISSUE_UPDATED = "issue.updated";
    String ISSUE_DELETED = "issue.deleted";

}