package com.projecthub.module.project.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.dto.CreateProjectRequest;
import com.projecthub.module.project.dto.ProjectMemberDTO;
import com.projecthub.module.project.dto.ProjectVO;
import com.projecthub.module.project.dto.UpdateProjectRequest;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.entity.ProjectMember;
import com.projecthub.module.project.repository.ProjectMemberRepository;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final PermissionService permissionService;

    /**
     * 创建项目
     */
    @Transactional
    public ProjectVO createProject(CreateProjectRequest request) {
        // 获取当前用户
        Long ownerId = getCurrentUserId();

        // 验证日期
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        // 创建项目
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setOwnerId(ownerId);
        project.setStatus(Project.ProjectStatus.ACTIVE);
        project.setIcon(request.getIcon());
        project.setThemeColor(request.getThemeColor());

        projectRepository.save(project);
        log.info("创建项目成功：projectId={}, ownerId={}", project.getId(), ownerId);

        return BeanCopyUtil.copyProperties(project, ProjectVO.class);
    }

    /**
     * 获取项目详情
     */
    @Transactional(readOnly = true)
    public ProjectVO getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        return BeanCopyUtil.copyProperties(project, ProjectVO.class);
    }

    /**
     * 更新项目
     */
    @Transactional
    public ProjectVO updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 权限校验：只有项目所有者可以更新
        if (!project.getOwnerId().equals(getCurrentUserId())) {
            throw new BusinessException(403, "只有项目所有者可以更新项目");
        }

        // 验证日期
        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        // 更新字段
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));
        }
        if (request.getIcon() != null) {
            project.setIcon(request.getIcon());
        }
        if (request.getThemeColor() != null) {
            project.setThemeColor(request.getThemeColor());
        }

        projectRepository.save(project);
        log.info("更新项目成功：projectId={}", projectId);

        return BeanCopyUtil.copyProperties(project, ProjectVO.class);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 权限校验：只有项目所有者可以删除
        if (!project.getOwnerId().equals(getCurrentUserId())) {
            throw new BusinessException(403, "只有项目所有者可以删除项目");
        }

        projectRepository.delete(project);
        log.info("删除项目成功：projectId={}", projectId);
    }

    /**
     * 获取项目列表（用户参与的）
     */
    @Transactional(readOnly = true)
    public PageResult<ProjectVO> getUserProjects(Integer page, Integer size, String keyword) {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 查询用户参与的项目 ID 列表
        List<Long> projectIds = permissionService.getUserProjectIds(userId);

        if (projectIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, page, size);
        }

        // 查询项目
        org.springframework.data.domain.Page<Project> projectPage;
        if (keyword != null && !keyword.isEmpty()) {
            projectPage = projectRepository.findAll((root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                predicates.add(root.get("id").in(projectIds));
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
                return query.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])).getRestriction();
            }, pageable);
        } else {
            projectPage = projectRepository.findAll((root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                predicates.add(root.get("id").in(projectIds));
                predicates.add(cb.isNull(root.get("deletedAt")));
                return query.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])).getRestriction();
            }, pageable);
        }

        List<ProjectVO> content = projectPage.getContent().stream()
                .map(project -> BeanCopyUtil.copyProperties(project, ProjectVO.class))
                .collect(Collectors.toList());

        return PageResult.of(content, projectPage.getTotalElements(), page, size);
    }

    /**
     * 添加项目成员
     */
    @Transactional
    public void addProjectMember(Long projectId, ProjectMemberDTO request) {
        // 权限校验
        checkProjectPermission(projectId, "PROJECT_MEMBER_MANAGE");

        // 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 检查用户是否已是成员
        if (memberRepository.findByProjectIdAndUserId(projectId, request.getUserId()).isPresent()) {
            throw new BusinessException("该用户已是项目成员");
        }

        // 添加成员
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRole(ProjectMember.ProjectMemberRole.valueOf(request.getRole()));

        memberRepository.save(member);
        log.info("添加项目成员成功：projectId={}, userId={}, role={}", projectId, request.getUserId(), request.getRole());
    }

    /**
     * 移除项目成员
     */
    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        // 权限校验
        checkProjectPermission(projectId, "PROJECT_MEMBER_MANAGE");

        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
        log.info("移除项目成员成功：projectId={}, userId={}", projectId, userId);
    }

    /**
     * 获取项目成员列表
     */
    @Transactional(readOnly = true)
    public List<ProjectMember> getProjectMembers(Long projectId) {
        return memberRepository.findByProjectId(projectId);
    }

    /**
     * 检查项目权限
     */
    private void checkProjectPermission(Long projectId, String permissionCode) {
        Long userId = getCurrentUserId();

        // 检查是否是项目所有者
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        if (project.getOwnerId().equals(userId)) {
            return; // 所有者拥有全部权限
        }

        // 检查是否是项目成员
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(403, "无项目访问权限"));

        // 根据角色判断权限
        if (permissionCode.equals("PROJECT_MEMBER_MANAGE") && member.getRole() != ProjectMember.ProjectMemberRole.OWNER) {
            throw new BusinessException(403, "权限不足");
        }
    }

    /**
     * 获取当前用户 ID
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        throw new BusinessException("用户未登录");
    }
}
