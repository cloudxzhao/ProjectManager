package com.projecthub.issue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.issue.dto.*;
import com.projecthub.issue.entity.Issue;
import com.projecthub.issue.event.IssueCreatedEventData;
import com.projecthub.issue.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public IssueVO createIssue(CreateIssueRequest request) {
        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStepsToReproduce(request.getStepsToReproduce());
        issue.setEnvironment(request.getEnvironment());
        issue.setProjectId(request.getProjectId());
        issue.setEpicId(request.getEpicId());
        issue.setTaskId(request.getTaskId());
        issue.setAssigneeId(request.getAssigneeId());
        issue.setReporterId(request.getReporterId());
        issue.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        issue.setSeverity(request.getSeverity() != null ? request.getSeverity() : "NORMAL");
        issue.setType(request.getType() != null ? request.getType() : "BUG");
        issue.setStoryPoints(request.getStoryPoints());
        issue.setParentIssueId(request.getParentIssueId());
        issue.setIssueKey(generateIssueKey(request.getProjectId()));
        issue.setStatus("OPEN");
        issue.setCreatorId(getCurrentUserId());

        if (request.getDueDate() != null) {
            issue.setDueDate(LocalDateTime.parse(request.getDueDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        issueRepository.insert(issue);

        // 发布问题创建事件
        publishIssueCreatedEvent(issue);

        return convertToIssueVO(issue);
    }

    public IssueVO getIssueById(Long id) {
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("Issue not found: " + id);
        }
        return convertToIssueVO(issue);
    }

    public List<IssueVO> getIssuesByProjectId(Long projectId) {
        List<Issue> issues = issueRepository.findByProjectId(projectId);
        return issues.stream()
                .map(this::convertToIssueVO)
                .collect(Collectors.toList());
    }

    public List<IssueVO> getIssuesByTaskId(Long taskId) {
        List<Issue> issues = issueRepository.findByTaskId(taskId);
        return issues.stream()
                .map(this::convertToIssueVO)
                .collect(Collectors.toList());
    }

    public IPage<IssueVO> getIssuesPage(Long projectId, int pageNum, int pageSize) {
        Page<Issue> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Issue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Issue::getProjectId, projectId)
                .eq(Issue::getDeleted, 0)
                .orderByDesc(Issue::getCreatedAt);

        IPage<Issue> issuePage = issueRepository.selectPage(page, wrapper);
        return issuePage.convert(this::convertToIssueVO);
    }

    @Transactional
    public IssueVO updateIssue(Long id, CreateIssueRequest request) {
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("Issue not found: " + id);
        }

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getStepsToReproduce() != null) {
            issue.setStepsToReproduce(request.getStepsToReproduce());
        }
        if (request.getEnvironment() != null) {
            issue.setEnvironment(request.getEnvironment());
        }
        if (request.getEpicId() != null) {
            issue.setEpicId(request.getEpicId());
        }
        if (request.getTaskId() != null) {
            issue.setTaskId(request.getTaskId());
        }
        if (request.getAssigneeId() != null) {
            issue.setAssigneeId(request.getAssigneeId());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getSeverity() != null) {
            issue.setSeverity(request.getSeverity());
        }
        if (request.getType() != null) {
            issue.setType(request.getType());
        }
        if (request.getStoryPoints() != null) {
            issue.setStoryPoints(request.getStoryPoints());
        }

        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.updateById(issue);
        return convertToIssueVO(issue);
    }

    @Transactional
    public IssueVO updateIssueStatus(Long id, String status) {
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("Issue not found: " + id);
        }

        issue.setStatus(status);
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            issue.setResolvedAt(LocalDateTime.now());
        }
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.updateById(issue);
        return convertToIssueVO(issue);
    }

    @Transactional
    public void deleteIssue(Long id) {
        issueRepository.deleteById(id);
    }

    // Private helper methods

    private String generateIssueKey(Long projectId) {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("ISSUE-%s-%s", projectId, uuid);
    }

    private Long getCurrentUserId() {
        // TODO: Get from UserContextHolder
        return 1L;
    }

    private IssueVO convertToIssueVO(Issue issue) {
        IssueVO vo = new IssueVO();
        vo.setId(issue.getId());
        vo.setIssueKey(issue.getIssueKey());
        vo.setTitle(issue.getTitle());
        vo.setDescription(issue.getDescription());
        vo.setStepsToReproduce(issue.getStepsToReproduce());
        vo.setEnvironment(issue.getEnvironment());
        vo.setProjectId(issue.getProjectId());
        vo.setEpicId(issue.getEpicId());
        vo.setTaskId(issue.getTaskId());
        vo.setAssigneeId(issue.getAssigneeId());
        vo.setReporterId(issue.getReporterId());
        vo.setStatus(issue.getStatus());
        vo.setPriority(issue.getPriority());
        vo.setSeverity(issue.getSeverity());
        vo.setType(issue.getType());
        vo.setStoryPoints(issue.getStoryPoints());
        vo.setParentIssueId(issue.getParentIssueId());
        vo.setResolvedAt(issue.getResolvedAt());
        vo.setDueDate(issue.getDueDate());
        vo.setCreatorId(issue.getCreatorId());
        vo.setCreatedAt(issue.getCreatedAt());
        vo.setUpdatedAt(issue.getUpdatedAt());

        // Count sub-issues
        LambdaQueryWrapper<Issue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Issue::getParentIssueId, issue.getId())
                .eq(Issue::getDeleted, 0);
        Long count = issueRepository.selectCount(wrapper);
        vo.setSubIssueCount(count.intValue());

        return vo;
    }

    /**
     * 发布问题创建事件
     */
    private void publishIssueCreatedEvent(Issue issue) {
        try {
            IssueCreatedEventData eventData = new IssueCreatedEventData();
            eventData.setIssueId(issue.getId());
            eventData.setIssueKey(issue.getIssueKey());
            eventData.setTitle(issue.getTitle());
            eventData.setType(issue.getType());
            eventData.setPriority(issue.getPriority());
            eventData.setSeverity(issue.getSeverity());
            eventData.setProjectId(issue.getProjectId());
            eventData.setAssigneeId(issue.getAssigneeId());
            eventData.setReporterId(issue.getReporterId());
            eventData.setCreatorId(issue.getCreatorId());
            eventData.setCreatedAt(issue.getCreatedAt());

            EventMessage<IssueCreatedEventData> event = EventMessage.of(
                    EventType.ISSUE_CREATED,
                    "issue-service",
                    eventData
            );

            eventPublisher.publish("issue.created", event);
            log.info("已发布问题创建事件：issueId={}, issueKey={}", issue.getId(), issue.getIssueKey());
        } catch (Exception e) {
            log.error("发布问题创建事件失败：issueId={}", issue.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}