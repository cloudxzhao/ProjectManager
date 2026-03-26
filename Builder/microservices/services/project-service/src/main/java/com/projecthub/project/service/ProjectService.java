package com.projecthub.project.service;

import com.projecthub.common.api.result.Result;
import com.projecthub.common.core.exception.BusinessException;
import com.projecthub.common.mq.constant.EventType;
import com.projecthub.common.mq.domain.EventMessage;
import com.projecthub.common.mq.service.EventPublisher;
import com.projecthub.common.security.util.UserContextHolder;
import com.projecthub.project.client.UserClient;
import com.projecthub.project.dto.*;
import com.projecthub.project.entity.Project;
import com.projecthub.project.entity.ProjectMember;
import com.projecthub.project.repository.ProjectMemberRepository;
import com.projecthub.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserClient userClient;
    private final EventPublisher eventPublisher;

    /**
     * 创建项目
     */
    @Transactional
    public ProjectVO createProject(CreateProjectRequest request) {
        Long currentUserId = UserContextHolder.getUserId();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setIcon(request.getIcon());
        project.setColor(request.getColor());
        project.setOwnerId(request.getOwnerId() != null ? request.getOwnerId() : currentUserId);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus("ACTIVE");
        project.setProgress(0);
        project.setCreatorId(currentUserId);
        project.setDeleted(0);

        projectRepository.insert(project);
        log.info("项目创建成功: {}", project.getId());

        // 添加创建者为项目成员（OWNER）
        ProjectMember owner = new ProjectMember();
        owner.setProjectId(project.getId());
        owner.setUserId(project.getOwnerId());
        owner.setRole("OWNER");
        projectMemberRepository.insert(owner);

        // 添加其他成员
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(project.getOwnerId())) {
                    ProjectMember member = new ProjectMember();
                    member.setProjectId(project.getId());
                    member.setUserId(memberId);
                    member.setRole("MEMBER");
                    projectMemberRepository.insert(member);
                }
            }
        }

        // 发布项目创建事件
        EventMessage<Long> event = EventMessage.of(
                EventType.PROJECT_CREATED,
                "project-service",
                project.getId()
        );
        eventPublisher.publish("project.created", event);

        return toVO(project);
    }

    /**
     * 更新项目
     */
    @Transactional
    public ProjectVO updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new BusinessException(3001, "项目不存在");
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            project.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getProgress() != null) {
            project.setProgress(request.getProgress());
        }

        projectRepository.updateById(project);
        log.info("项目更新成功: {}", id);

        // 发布项目更新事件
        EventMessage<Long> event = EventMessage.of(
                EventType.PROJECT_UPDATED,
                "project-service",
                id
        );
        eventPublisher.publish("project.updated", event);

        return toVO(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new BusinessException(3001, "项目不存在");
        }

        // 删除项目成员
        List<ProjectMember> members = projectMemberRepository.findByProjectId(id);
        for (ProjectMember member : members) {
            projectMemberRepository.deleteById(member.getId());
        }

        // 删除项目
        projectRepository.deleteById(id);
        log.info("项目删除成功: {}", id);

        // 发布项目删除事件
        EventMessage<Long> event = EventMessage.of(
                EventType.PROJECT_DELETED,
                "project-service",
                id
        );
        eventPublisher.publish("project.deleted", event);
    }

    /**
     * 获取项目详情
     */
    public ProjectVO getProjectById(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new BusinessException(3001, "项目不存在");
        }
        return toVO(project);
    }

    /**
     * 获取所有项目
     */
    public List<ProjectVO> getAllProjects() {
        return projectRepository.selectList(null).stream()
                .filter(p -> p.getDeleted() == 0)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态获取项目
     */
    public List<ProjectVO> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取当前用户的项目
     */
    public List<ProjectVO> getMyProjects() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            return List.of();
        }
        return projectRepository.findByMemberId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 添加项目成员
     */
    @Transactional
    public List<ProjectMemberVO> addMembers(Long projectId, AddMembersRequest request) {
        Project project = projectRepository.selectById(projectId);
        if (project == null) {
            throw new BusinessException(3001, "项目不存在");
        }

        List<ProjectMemberVO> addedMembers = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            // 检查是否已是成员
            if (projectMemberRepository.isMember(projectId, userId)) {
                continue;
            }

            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(userId);
            member.setRole(request.getRole());
            projectMemberRepository.insert(member);

            addedMembers.add(toMemberVO(member));

            // 发布成员添加事件
            EventMessage<Map<String, Object>> event = EventMessage.of(
                    EventType.PROJECT_MEMBER_ADDED,
                    "project-service",
                    Map.of("projectId", projectId, "userId", userId)
            );
            eventPublisher.publish("project.member.added", event);
        }

        log.info("添加项目成员成功: projectId={}, count={}", projectId, addedMembers.size());
        return addedMembers;
    }

    /**
     * 移除项目成员
     */
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(3004, "成员不存在"));

        if ("OWNER".equals(member.getRole())) {
            throw new BusinessException(400, "不能移除项目所有者");
        }

        projectMemberRepository.deleteById(member.getId());
        log.info("移除项目成员: projectId={}, userId={}", projectId, userId);

        // 发布成员移除事件
        EventMessage<Map<String, Object>> event = EventMessage.of(
                EventType.PROJECT_MEMBER_REMOVED,
                "project-service",
                Map.of("projectId", projectId, "userId", userId)
        );
        eventPublisher.publish("project.member.removed", event);
    }

    /**
     * 获取项目成员列表
     */
    public List<ProjectMemberVO> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        // 批量获取用户信息
        List<Long> userIds = members.stream().map(ProjectMember::getUserId).collect(Collectors.toList());
        Map<Long, Map<String, Object>> userMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            try {
                Result<List<Map<String, Object>>> result = userClient.getUsersByIds(userIds);
                if (result != null && result.getData() != null) {
                    userMap = result.getData().stream()
                            .collect(Collectors.toMap(u -> ((Number) u.get("id")).longValue(), u -> u));
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: {}", e.getMessage());
            }
        }

        Map<Long, Map<String, Object>> finalUserMap = userMap;
        return members.stream()
                .map(m -> toMemberVOWithUser(m, finalUserMap.get(m.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否是项目成员
     */
    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.isMember(projectId, userId);
    }

    /**
     * 实体转VO
     */
    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setIcon(project.getIcon());
        vo.setColor(project.getColor());
        vo.setOwnerId(project.getOwnerId());
        vo.setStartDate(project.getStartDate());
        vo.setEndDate(project.getEndDate());
        vo.setStatus(project.getStatus());
        vo.setProgress(project.getProgress());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setMemberCount(projectMemberRepository.countByProjectId(project.getId()));

        // 获取负责人名称
        try {
            Result<Map<String, Object>> result = userClient.getUserById(project.getOwnerId());
            if (result != null && result.getData() != null) {
                vo.setOwnerName((String) result.getData().get("username"));
            }
        } catch (Exception e) {
            log.warn("获取负责人信息失败: {}", e.getMessage());
        }

        return vo;
    }

    /**
     * 成员转VO
     */
    private ProjectMemberVO toMemberVO(ProjectMember member) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setId(member.getId());
        vo.setProjectId(member.getProjectId());
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setJoinedAt(member.getJoinedAt());
        return vo;
    }

    /**
     * 成员转VO（带用户信息）
     */
    private ProjectMemberVO toMemberVOWithUser(ProjectMember member, Map<String, Object> user) {
        ProjectMemberVO vo = toMemberVO(member);
        if (user != null) {
            vo.setUsername((String) user.get("username"));
            vo.setEmail((String) user.get("email"));
            vo.setAvatar((String) user.get("avatar"));
        }
        return vo;
    }

}