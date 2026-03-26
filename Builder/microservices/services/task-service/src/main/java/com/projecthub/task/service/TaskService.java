package com.projecthub.task.service;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.task.client.ProjectClient;
import com.projecthub.task.client.UserClient;
import com.projecthub.task.dto.*;
import com.projecthub.task.entity.Comment;
import com.projecthub.task.entity.Task;
import com.projecthub.task.repository.CommentRepository;
import com.projecthub.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final ProjectClient projectClient;
    private final EventPublisher eventPublisher;

    /**
     * 创建任务
     */
    @Transactional
    public TaskVO createTask(CreateTaskRequest request) {
        Long currentUserId = UserContextHolder.getUserId();

        // 检查项目是否存在
        try {
            Result<Map<String, Object>> projectResult = projectClient.getProjectById(request.getProjectId());
            if (projectResult == null || projectResult.getData() == null) {
                throw new BusinessException(3001, "项目不存在");
            }
        } catch (Exception e) {
            log.warn("检查项目失败: {}", e.getMessage());
        }

        // 生成任务编号
        int taskNumber = taskRepository.getNextTaskNumber(request.getProjectId());
        String taskKey = String.format("TASK-%03d", taskNumber);

        Task task = new Task();
        task.setTaskKey(taskKey);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProjectId(request.getProjectId());
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(currentUserId);
        task.setStatus("TODO");
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setDueDate(request.getDueDate());
        task.setSortOrder(0);
        task.setDeleted(0);

        taskRepository.insert(task);
        log.info("任务创建成功: {}", task.getTaskKey());

        // 发布任务创建事件
        EventMessage<Map<String, Object>> event = EventMessage.of(
                EventType.TASK_CREATED,
                "task-service",
                Map.of("taskId", task.getId(), "projectId", task.getProjectId())
        );
        eventPublisher.publish("task.created", event);

        return toVO(task);
    }

    /**
     * 更新任务
     */
    @Transactional
    public TaskVO updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.selectById(id);
        if (task == null) {
            throw new BusinessException(4001, "任务不存在");
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getAssigneeId() != null) {
            Long oldAssignee = task.getAssigneeId();
            task.setAssigneeId(request.getAssigneeId());

            // 发布任务分配事件
            EventMessage<Map<String, Object>> event = EventMessage.of(
                    EventType.TASK_ASSIGNED,
                    "task-service",
                    Map.of("taskId", id, "oldAssignee", oldAssignee != null ? oldAssignee : 0, "newAssignee", request.getAssigneeId())
            );
            eventPublisher.publish("task.assigned", event);
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        taskRepository.updateById(task);
        log.info("任务更新成功: {}", id);

        return toVO(task);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.selectById(id);
        if (task == null) {
            throw new BusinessException(4001, "任务不存在");
        }

        taskRepository.deleteById(id);
        log.info("任务删除成功: {}", id);
    }

    /**
     * 移动任务（改变状态/排序）
     */
    @Transactional
    public TaskVO moveTask(Long id, MoveTaskRequest request) {
        Task task = taskRepository.selectById(id);
        if (task == null) {
            throw new BusinessException(4001, "任务不存在");
        }

        String oldStatus = task.getStatus();
        taskRepository.updatePosition(id, request.getStatus(), request.getSortOrder(), request.getColumnId());

        task.setStatus(request.getStatus());
        task.setSortOrder(request.getSortOrder());

        // 发布状态变更事件
        if (!oldStatus.equals(request.getStatus())) {
            EventMessage<Map<String, Object>> event = EventMessage.of(
                    EventType.TASK_STATUS_CHANGED,
                    "task-service",
                    Map.of("taskId", id, "oldStatus", oldStatus, "newStatus", request.getStatus())
            );
            eventPublisher.publish("task.status.changed", event);
        }

        log.info("任务移动成功: {} -> {}", id, request.getStatus());
        return toVO(task);
    }

    /**
     * 获取任务详情
     */
    public TaskVO getTaskById(Long id) {
        Task task = taskRepository.selectById(id);
        if (task == null) {
            throw new BusinessException(4001, "任务不存在");
        }
        return toVO(task);
    }

    /**
     * 获取项目的任务列表
     */
    public List<TaskVO> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取项目看板数据（按状态分组）
     */
    public Map<String, List<TaskVO>> getKanbanData(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(TaskVO::getStatus));
    }

    /**
     * 获取我的任务
     */
    public List<TaskVO> getMyTasks() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            return List.of();
        }
        return taskRepository.findByAssigneeId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 添加评论
     */
    @Transactional
    public CommentVO addComment(Long taskId, CreateCommentRequest request) {
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new BusinessException(4001, "任务不存在");
        }

        Long currentUserId = UserContextHolder.getUserId();

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(currentUserId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setDeleted(0);

        commentRepository.insert(comment);
        log.info("评论添加成功: taskId={}", taskId);

        // 发布评论事件
        EventMessage<Map<String, Object>> event = EventMessage.of(
                EventType.COMMENT_CREATED,
                "task-service",
                Map.of("commentId", comment.getId(), "taskId", taskId)
        );
        eventPublisher.publish("comment.created", event);

        return toCommentVO(comment);
    }

    /**
     * 获取任务评论
     */
    public List<CommentVO> getComments(Long taskId) {
        List<Comment> comments = commentRepository.findByTaskId(taskId);

        // 批量获取用户信息
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, Map<String, Object>> userMap = getUserMap(userIds);

        return comments.stream()
                .map(c -> toCommentVOWithUser(c, userMap.get(c.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        Comment comment = commentRepository.selectById(commentId);
        if (comment == null || !comment.getTaskId().equals(taskId)) {
            throw new BusinessException(400, "评论不存在");
        }

        commentRepository.deleteById(commentId);
        log.info("评论删除成功: {}", commentId);
    }

    // === 私有方法 ===

    private TaskVO toVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTaskKey(task.getTaskKey());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setProjectId(task.getProjectId());
        vo.setAssigneeId(task.getAssigneeId());
        vo.setCreatorId(task.getCreatorId());
        vo.setStatus(task.getStatus());
        vo.setPriority(task.getPriority());
        vo.setStoryPoints(task.getStoryPoints());
        vo.setDueDate(task.getDueDate());
        vo.setSortOrder(task.getSortOrder());
        vo.setCreatedAt(task.getCreatedAt());

        // 获取负责人名称
        if (task.getAssigneeId() != null) {
            try {
                Result<Map<String, Object>> result = userClient.getUserById(task.getAssigneeId());
                if (result != null && result.getData() != null) {
                    vo.setAssigneeName((String) result.getData().get("username"));
                }
            } catch (Exception e) {
                log.warn("获取负责人信息失败: {}", e.getMessage());
            }
        }

        // 获取项目名称
        try {
            Result<Map<String, Object>> result = projectClient.getProjectById(task.getProjectId());
            if (result != null && result.getData() != null) {
                vo.setProjectName((String) result.getData().get("name"));
            }
        } catch (Exception e) {
            log.warn("获取项目信息失败: {}", e.getMessage());
        }

        return vo;
    }

    private CommentVO toCommentVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }

    private CommentVO toCommentVOWithUser(Comment comment, Map<String, Object> user) {
        CommentVO vo = toCommentVO(comment);
        if (user != null) {
            vo.setUsername((String) user.get("username"));
            vo.setAvatar((String) user.get("avatar"));
        }
        return vo;
    }

    private Map<Long, Map<String, Object>> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Result<List<Map<String, Object>>> result = userClient.getUsersByIds(userIds);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .collect(Collectors.toMap(u -> ((Number) u.get("id")).longValue(), u -> u));
            }
        } catch (Exception e) {
            log.warn("批量获取用户信息失败: {}", e.getMessage());
        }
        return Map.of();
    }

}