package com.projecthub.common.mq.constant;

/**
 * 事件类型常量
 */
public interface EventTypes {

    // ========== 用户事件 ==========
    /**
     * 用户创建
     */
    String USER_CREATED = "user.created";

    /**
     * 用户信息更新
     */
    String USER_UPDATED = "user.updated";

    /**
     * 用户删除
     */
    String USER_DELETED = "user.deleted";

    // ========== 项目事件 ==========
    /**
     * 项目创建
     */
    String PROJECT_CREATED = "project.created";

    /**
     * 项目更新
     */
    String PROJECT_UPDATED = "project.updated";

    /**
     * 项目删除
     */
    String PROJECT_DELETED = "project.deleted";

    /**
     * 项目成员添加
     */
    String PROJECT_MEMBER_ADDED = "project.member.added";

    /**
     * 项目成员移除
     */
    String PROJECT_MEMBER_REMOVED = "project.member.removed";

    // ========== 任务事件 ==========
    /**
     * 任务创建
     */
    String TASK_CREATED = "task.created";

    /**
     * 任务更新
     */
    String TASK_UPDATED = "task.updated";

    /**
     * 任务删除
     */
    String TASK_DELETED = "task.deleted";

    /**
     * 任务分配
     */
    String TASK_ASSIGNED = "task.assigned";

    /**
     * 任务状态变更
     */
    String TASK_STATUS_CHANGED = "task.status.changed";

    // ========== 用户故事事件 ==========
    /**
     * 用户故事创建
     */
    String STORY_CREATED = "story.created";

    /**
     * 用户故事更新
     */
    String STORY_UPDATED = "story.updated";

    /**
     * 用户故事删除
     */
    String STORY_DELETED = "story.deleted";

    // ========== 问题事件 ==========
    /**
     * 问题创建
     */
    String ISSUE_CREATED = "issue.created";

    /**
     * 问题更新
     */
    String ISSUE_UPDATED = "issue.updated";

    /**
     * 问题删除
     */
    String ISSUE_DELETED = "issue.deleted";

    // ========== Wiki 事件 ==========
    /**
     * Wiki 文档创建
     */
    String WIKI_CREATED = "wiki.created";

    /**
     * Wiki 文档更新
     */
    String WIKI_UPDATED = "wiki.updated";

    /**
     * Wiki 文档删除
     */
    String WIKI_DELETED = "wiki.deleted";

    // ========== 通知事件 ==========
    /**
     * 站内通知
     */
    String NOTIFICATION_INBOX = "notification.inbox";

    /**
     * 邮件通知
     */
    String NOTIFICATION_EMAIL = "notification.email";

}
