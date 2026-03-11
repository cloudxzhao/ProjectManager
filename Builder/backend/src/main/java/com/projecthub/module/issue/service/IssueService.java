package com.projecthub.module.issue.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.issue.dto.IssueVO;
import com.projecthub.module.issue.entity.Issue;
import com.projecthub.module.issue.repository.IssueRepository;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 问题服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

  private final IssueRepository issueRepository;
  private final UserRepository userRepository;
  private final PermissionService permissionService;

  /** 创建问题 */
  @Transactional
  public IssueVO createIssue(Long projectId, IssueVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "ISSUE_CREATE")) {
      throw new BusinessException(403, "无创建问题权限");
    }

    // 创建问题
    Issue issue =
        Issue.builder()
            .projectId(projectId)
            .title(request.getTitle())
            .description(request.getDescription())
            .type(
                request.getType() != null
                    ? Issue.IssueType.valueOf(request.getType())
                    : Issue.IssueType.BUG)
            .severity(
                request.getSeverity() != null
                    ? Issue.Severity.valueOf(request.getSeverity())
                    : Issue.Severity.NORMAL)
            .assigneeId(request.getAssigneeId())
            .reporterId(userId)
            .foundDate(request.getFoundDate() != null ? request.getFoundDate() : LocalDate.now())
            .build();

    issueRepository.save(issue);
    log.info("创建问题成功：issueId={}, projectId={}", issue.getId(), projectId);

    return buildIssueVO(issue);
  }

  /** 获取问题详情 */
  @Transactional(readOnly = true)
  public IssueVO getIssue(Long issueId) {
    Issue issue =
        issueRepository.findById(issueId).orElseThrow(() -> new BusinessException("问题不存在"));

    return buildIssueVO(issue);
  }

  /** 获取项目下的问题列表 */
  @Transactional(readOnly = true)
  public PageResult<IssueVO> listIssues(
      Long projectId, IssueVO.FilterRequest filter, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Specification<Issue> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 项目 ID 筛选
          predicates.add(cb.equal(root.get("projectId"), projectId));

          // 类型筛选
          if (filter != null && filter.getType() != null) {
            predicates.add(cb.equal(root.get("type"), Issue.IssueType.valueOf(filter.getType())));
          }

          // 严重程度筛选
          if (filter != null && filter.getSeverity() != null) {
            predicates.add(
                cb.equal(root.get("severity"), Issue.Severity.valueOf(filter.getSeverity())));
          }

          // 状态筛选
          if (filter != null && filter.getStatus() != null) {
            predicates.add(
                cb.equal(root.get("status"), Issue.IssueStatus.valueOf(filter.getStatus())));
          }

          // 负责人筛选
          if (filter != null && filter.getAssigneeId() != null) {
            predicates.add(cb.equal(root.get("assigneeId"), filter.getAssigneeId()));
          }

          // 标题关键字筛选
          if (filter != null && StringUtils.hasText(filter.getKeyword())) {
            predicates.add(cb.like(root.get("title"), "%" + filter.getKeyword() + "%"));
          }

          return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

    Page<Issue> issuePage = issueRepository.findAll(spec, pageable);

    List<IssueVO> content =
        issuePage.getContent().stream().map(this::buildIssueVO).collect(Collectors.toList());

    return PageResult.of(content, issuePage.getTotalElements(), page, size);
  }

  /** 更新问题 */
  @Transactional
  public IssueVO updateIssue(Long issueId, IssueVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new BusinessException("问题不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, issue.getProjectId(), "ISSUE_EDIT")) {
      throw new BusinessException(403, "无编辑问题权限");
    }

    // 更新字段
    if (request.getTitle() != null) {
      issue.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      issue.setDescription(request.getDescription());
    }
    if (request.getType() != null) {
      issue.setType(Issue.IssueType.valueOf(request.getType()));
    }
    if (request.getSeverity() != null) {
      issue.setSeverity(Issue.Severity.valueOf(request.getSeverity()));
    }
    if (request.getStatus() != null) {
      issue.setStatus(Issue.IssueStatus.valueOf(request.getStatus()));
    }
    if (request.getAssigneeId() != null) {
      issue.setAssigneeId(request.getAssigneeId());
    }
    if (request.getFoundDate() != null) {
      issue.setFoundDate(request.getFoundDate());
    }
    if (request.getResolvedDate() != null) {
      issue.setResolvedDate(request.getResolvedDate());
    }

    issueRepository.save(issue);
    log.info("更新问题成功：issueId={}", issueId);

    return buildIssueVO(issue);
  }

  /** 删除问题 */
  @Transactional
  public void deleteIssue(Long issueId) {
    Long userId = getCurrentUserId();

    Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new BusinessException("问题不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, issue.getProjectId(), "ISSUE_DELETE")) {
      throw new BusinessException(403, "无删除问题权限");
    }

    issueRepository.delete(issue);
    log.info("删除问题成功：issueId={}", issueId);
  }

  /** 构建问题 VO */
  private IssueVO buildIssueVO(Issue issue) {
    IssueVO vo = BeanCopyUtil.copyProperties(issue, IssueVO.class);

    // 查询负责人姓名
    if (issue.getAssigneeId() != null) {
      userRepository
          .findById(issue.getAssigneeId())
          .ifPresent(user -> vo.setAssigneeName(user.getUsername()));
    }

    // 查询报告人姓名
    userRepository
        .findById(issue.getReporterId())
        .ifPresent(user -> vo.setReporterName(user.getUsername()));

    return vo;
  }

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getId();
    }
    throw new BusinessException("用户未登录");
  }
}