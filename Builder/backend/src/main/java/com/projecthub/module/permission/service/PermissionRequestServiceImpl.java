package com.projecthub.module.permission.service;

import com.projecthub.common.constant.ErrorCode;
import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.response.PageResult;
import com.projecthub.module.permission.dto.ApprovePermissionRequestDTO;
import com.projecthub.module.permission.dto.CreatePermissionRequestDTO;
import com.projecthub.module.permission.entity.PermissionApproval;
import com.projecthub.module.permission.entity.PermissionApproval.ApprovalAction;
import com.projecthub.module.permission.entity.PermissionRequest;
import com.projecthub.module.permission.entity.PermissionRequest.RequestStatus;
import com.projecthub.module.permission.repository.PermissionApprovalRepository;
import com.projecthub.module.permission.repository.PermissionRequestRepository;
import com.projecthub.module.permission.vo.AvailablePermissionVO;
import com.projecthub.module.permission.vo.PermissionApprovalVO;
import com.projecthub.module.permission.vo.PermissionRequestVO;
import com.projecthub.module.project.entity.Project;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.user.entity.SysPermission;
import com.projecthub.module.user.entity.SysRole;
import com.projecthub.module.user.entity.SysRolePermission;
import com.projecthub.module.user.entity.SysUserRole;
import com.projecthub.module.user.entity.User;
import com.projecthub.module.user.repository.SysPermissionRepository;
import com.projecthub.module.user.repository.SysRolePermissionRepository;
import com.projecthub.module.user.repository.SysUserRoleRepository;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.security.UserDetailsImpl;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 权限申请服务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionRequestServiceImpl implements PermissionRequestService {

  private final PermissionRequestRepository permissionRequestRepository;
  private final PermissionApprovalRepository permissionApprovalRepository;
  private final SysPermissionRepository sysPermissionRepository;
  private final SysRolePermissionRepository sysRolePermissionRepository;
  private final SysUserRoleRepository sysUserRoleRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  @Transactional(readOnly = true)
  public List<AvailablePermissionVO> getAvailablePermissions() {
    Long userId = getCurrentUserId();

    // 查询用户已有的权限 ID 列表
    List<Long> userRoleIds = sysUserRoleRepository.findRoleIdsByUserId(userId);
    List<Long> userPermissionIds =
        sysRolePermissionRepository.findPermissionIdsByRoleIds(userRoleIds);
    Set<Long> userPermissionIdSet = new HashSet<>(userPermissionIds);

    // 查询所有权限
    List<SysPermission> allPermissions = sysPermissionRepository.findAll();

    return allPermissions.stream()
        .map(
            permission ->
                AvailablePermissionVO.builder()
                    .id(permission.getId())
                    .name(permission.getName())
                    .code(permission.getCode())
                    .description(permission.getDescription())
                    .hasPermission(userPermissionIdSet.contains(permission.getId()))
                    .build())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PermissionRequestVO createPermissionRequest(CreatePermissionRequestDTO dto) {
    Long userId = getCurrentUserId();
    log.info(
        "开始创建权限申请：userId={}, permissionId={}, reason={}",
        userId,
        dto.getPermissionId(),
        dto.getReason());

    // 验证权限是否存在
    SysPermission permission =
        sysPermissionRepository
            .findById(dto.getPermissionId())
            .orElseThrow(
                () -> {
                  log.error("权限不存在：permissionId={}", dto.getPermissionId());
                  return new BusinessException(ErrorCode.PERMISSION_NOT_FOUND, "权限不存在");
                });
    log.info("权限验证通过：permissionId={}, name={}", permission.getId(), permission.getName());

    // 验证项目是否存在（如果传入了 projectId）
    if (dto.getProjectId() != null) {
      projectRepository
          .findById(dto.getProjectId())
          .orElseThrow(
              () -> {
                log.error("项目不存在：projectId={}", dto.getProjectId());
                return new BusinessException(ErrorCode.PROJECT_NOT_FOUND, "项目不存在");
              });
    }

    // 检查是否已有待审批的相同申请
    List<PermissionRequest> pendingRequests =
        permissionRequestRepository.findPendingByUserIdAndPermissionId(
            userId, dto.getPermissionId());
    log.info("检查待审批申请：count={}", pendingRequests.size());
    if (!pendingRequests.isEmpty()) {
      throw new BusinessException(ErrorCode.PERMISSION_REQUEST_ALREADY_PROCESSED, "您已有待审批的相同权限申请");
    }

    // 创建申请
    PermissionRequest request =
        PermissionRequest.builder()
            .userId(userId)
            .permissionId(dto.getPermissionId())
            .projectId(dto.getProjectId())
            .reason(dto.getReason())
            .status(RequestStatus.PENDING)
            .build();
    log.info(
        "准备保存申请记录：userId={}, permissionId={}, reason={}",
        userId,
        dto.getPermissionId(),
        dto.getReason());

    try {
      permissionRequestRepository.save(request);
      log.info(
          "创建权限申请成功：requestId={}, userId={}, permissionId={}",
          request.getId(),
          userId,
          dto.getPermissionId());
    } catch (Exception e) {
      log.error(
          "保存权限申请失败：userId={}, permissionId={}, error={}",
          userId,
          dto.getPermissionId(),
          e.getMessage(),
          e);
      throw e;
    }

    return buildPermissionRequestVO(request);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<PermissionRequestVO> getPermissionRequests(
      Integer page, Integer size, String status, Long userId, Long permissionId) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    org.springframework.data.domain.Page<PermissionRequest> requestPage;

    // 根据参数构建查询
    if (userId != null) {
      // 查询指定用户的申请
      if (status != null && !status.isEmpty()) {
        requestPage =
            permissionRequestRepository.findByUserIdAndStatus(
                userId, RequestStatus.valueOf(status), pageable);
      } else {
        requestPage = permissionRequestRepository.findByUserId(userId, pageable);
      }
    } else if (permissionId != null) {
      // 查询指定权限的申请（管理员视角）
      requestPage =
          permissionRequestRepository.findAll(
              (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("permissionId"), permissionId));
                if (status != null && !status.isEmpty()) {
                  predicates.add(cb.equal(root.get("status"), status));
                }
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
              },
              pageable);
    } else if (status != null && !status.isEmpty()) {
      // 按状态查询（管理员视角）
      requestPage =
          permissionRequestRepository.findByStatus(RequestStatus.valueOf(status), pageable);
    } else {
      // 查询所有申请（管理员视角）
      requestPage = permissionRequestRepository.findAll(pageable);
    }

    List<PermissionRequestVO> content =
        requestPage.getContent().stream().map(this::buildPermissionRequestVO).toList();

    return PageResult.of(content, requestPage.getTotalElements(), page, size);
  }

  @Override
  @Transactional(readOnly = true)
  public PermissionRequestVO getPermissionRequestDetail(Long requestId) {
    PermissionRequest request =
        permissionRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.PERMISSION_REQUEST_NOT_FOUND, "权限申请记录不存在"));

    return buildPermissionRequestVO(request);
  }

  @Override
  @Transactional(readOnly = true)
  public PermissionRequestVO getPermissionRequestWithApprovals(Long requestId) {
    PermissionRequestVO requestVO = getPermissionRequestDetail(requestId);

    // 获取审批记录
    List<PermissionApprovalVO> approvalRecords = getApprovalRecords(requestId);
    requestVO.setApprovalRecords(approvalRecords);

    return requestVO;
  }

  @Override
  @Transactional
  public void approvePermissionRequest(Long requestId, ApprovePermissionRequestDTO dto) {
    Long approverId = getCurrentUserId();

    // 验证审批人是否为管理员
    User approver =
        userRepository
            .findById(approverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 检查管理员角色
    boolean isAdmin = checkIfAdmin(approverId);
    if (!isAdmin) {
      throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只有管理员可以审批权限申请");
    }

    // 获取申请记录
    PermissionRequest request =
        permissionRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.PERMISSION_REQUEST_NOT_FOUND, "权限申请记录不存在"));

    // 检查申请状态
    if (request.getStatus() != RequestStatus.PENDING) {
      throw new BusinessException(ErrorCode.PERMISSION_REQUEST_ALREADY_PROCESSED, "该申请已被处理，无法重复审批");
    }

    // 创建审批记录
    PermissionApproval approval =
        PermissionApproval.builder()
            .requestId(requestId)
            .approverId(approverId)
            .action(ApprovalAction.APPROVE)
            .comment(dto.getComment())
            .build();
    permissionApprovalRepository.save(approval);

    // 更新申请状态
    request.setStatus(RequestStatus.APPROVED);
    permissionRequestRepository.save(request);

    // 授予权限
    grantPermission(request.getUserId(), request.getPermissionId());

    log.info(
        "审批通过权限申请：requestId={}, applicantId={}, permissionId={}, approverId={}",
        requestId,
        request.getUserId(),
        request.getPermissionId(),
        approverId);
  }

  @Override
  @Transactional
  public void rejectPermissionRequest(Long requestId, ApprovePermissionRequestDTO dto) {
    Long approverId = getCurrentUserId();

    // 验证审批人是否为管理员
    boolean isAdmin = checkIfAdmin(approverId);
    if (!isAdmin) {
      throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只有管理员可以审批权限申请");
    }

    // 获取申请记录
    PermissionRequest request =
        permissionRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.PERMISSION_REQUEST_NOT_FOUND, "权限申请记录不存在"));

    // 检查申请状态
    if (request.getStatus() != RequestStatus.PENDING) {
      throw new BusinessException(ErrorCode.PERMISSION_REQUEST_ALREADY_PROCESSED, "该申请已被处理，无法重复审批");
    }

    // 创建审批记录
    PermissionApproval approval =
        PermissionApproval.builder()
            .requestId(requestId)
            .approverId(approverId)
            .action(ApprovalAction.REJECT)
            .comment(dto.getComment())
            .build();
    permissionApprovalRepository.save(approval);

    // 更新申请状态
    request.setStatus(RequestStatus.REJECTED);
    permissionRequestRepository.save(request);

    log.info(
        "审批拒绝权限申请：requestId={}, applicantId={}, permissionId={}, approverId={}",
        requestId,
        request.getUserId(),
        request.getPermissionId(),
        approverId);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<PermissionRequestVO> getMyPermissionRequests(
      Integer page, Integer size, String status) {
    Long userId = getCurrentUserId();
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    org.springframework.data.domain.Page<PermissionRequest> requestPage;

    if (status != null && !status.isEmpty()) {
      requestPage =
          permissionRequestRepository.findByUserIdAndStatus(
              userId, RequestStatus.valueOf(status), pageable);
    } else {
      requestPage = permissionRequestRepository.findByUserId(userId, pageable);
    }

    List<PermissionRequestVO> content =
        requestPage.getContent().stream().map(this::buildPermissionRequestVO).toList();

    return PageResult.of(content, requestPage.getTotalElements(), page, size);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PermissionApprovalVO> getApprovalRecords(Long requestId) {
    List<PermissionApproval> approvals =
        permissionApprovalRepository.findByRequestIdOrderByCreatedAtAsc(requestId);

    if (approvals.isEmpty()) {
      return List.of();
    }

    // 提取审批人 ID 列表
    List<Long> approverIds =
        approvals.stream().map(PermissionApproval::getApproverId).distinct().toList();

    // 批量查询审批人信息
    List<User> approvers = userRepository.findByIds(approverIds);

    return approvals.stream()
        .map(
            approval -> {
              User approver =
                  approvers.stream()
                      .filter(u -> u.getId().equals(approval.getApproverId()))
                      .findFirst()
                      .orElse(null);
              return PermissionApprovalVO.builder()
                  .id(approval.getId())
                  .requestId(approval.getRequestId())
                  .approverId(approval.getApproverId())
                  .approverName(approver != null ? approver.getUsername() : "")
                  .approverNickname(approver != null ? approver.getNickname() : "")
                  .action(approval.getAction().name())
                  .comment(approval.getComment())
                  .createdAt(approval.getCreatedAt())
                  .build();
            })
        .collect(Collectors.toList());
  }

  /** 构建 PermissionRequestVO */
  private PermissionRequestVO buildPermissionRequestVO(PermissionRequest request) {
    User user = userRepository.findById(request.getUserId()).orElse(null);
    SysPermission permission =
        sysPermissionRepository.findById(request.getPermissionId()).orElse(null);
    Project project = null;
    if (request.getProjectId() != null) {
      project = projectRepository.findById(request.getProjectId()).orElse(null);
    }

    return PermissionRequestVO.builder()
        .id(request.getId())
        .userId(request.getUserId())
        .username(user != null ? user.getUsername() : "")
        .nickname(user != null ? user.getNickname() : "")
        .avatar(user != null ? user.getAvatar() : "")
        .permissionId(request.getPermissionId())
        .permissionName(permission != null ? permission.getName() : "")
        .permissionCode(permission != null ? permission.getCode() : "")
        .projectId(request.getProjectId())
        .projectName(project != null ? project.getName() : "")
        .reason(request.getReason())
        .status(request.getStatus().name())
        .createdAt(request.getCreatedAt())
        .updatedAt(request.getUpdatedAt())
        .build();
  }

  /** 授予权限 */
  private void grantPermission(Long userId, Long permissionId) {
    log.info("开始授予权限：userId={}, permissionId={}", userId, permissionId);

    // 查找是否有角色已包含此权限，如果有则直接关联用户到该角色
    List<SysRolePermission> rolePermissions = sysRolePermissionRepository.findAll();
    log.info("查询到角色权限关联总数：{}", rolePermissions.size());

    Set<Long> roleIdsWithPermission =
        rolePermissions.stream()
            .filter(rp -> rp.getPermissionId().equals(permissionId))
            .map(SysRolePermission::getRoleId)
            .collect(Collectors.toSet());
    log.info("包含权限 {} 的角色 ID 列表：{}", permissionId, roleIdsWithPermission);

    if (!roleIdsWithPermission.isEmpty()) {
      // 获取用户当前的角色
      List<SysUserRole> userRoles = sysUserRoleRepository.findAllByUserId(userId);
      log.info("用户 {} 当前的角色关联：{}", userId, userRoles.size());

      Set<Long> userRoleIds =
          userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());

      // 找到用户已有角色中包含该权限的角色
      Set<Long> existingRoleIdsWithPermission =
          userRoleIds.stream().filter(roleIdsWithPermission::contains).collect(Collectors.toSet());
      log.info("用户已有角色中包含该权限的角色 ID 列表：{}", existingRoleIdsWithPermission);

      if (!existingRoleIdsWithPermission.isEmpty()) {
        // 用户已有角色包含该权限，无需额外操作
        log.info("用户已有角色包含该权限，无需额外授权：userId={}, permissionId={}", userId, permissionId);
        return;
      }

      // 将用户关联到第一个包含该权限的角色（如果用户没有任何角色）
      if (userRoles.isEmpty()) {
        Long targetRoleId = roleIdsWithPermission.iterator().next();
        log.info("准备为用户 {} 分配角色 {} (包含权限 {})", userId, targetRoleId, permissionId);
        SysUserRole userRole = SysUserRole.builder().userId(userId).roleId(targetRoleId).build();
        sysUserRoleRepository.save(userRole);
        log.info(
            "授予用户角色（包含该权限）：userId={}, roleId={}, permissionId={}",
            userId,
            targetRoleId,
            permissionId);
      }
    } else {
      log.warn("没有找到包含该权限的角色：permissionId={}", permissionId);
    }
  }

  /** 检查是否为管理员 */
  private boolean checkIfAdmin(Long userId) {
    List<Long> roleIds = sysUserRoleRepository.findRoleIdsByUserId(userId);
    if (roleIds.isEmpty()) {
      return false;
    }

    // 查找角色 code
    List<SysRole> roles = userRepository.findRolesByUserId(userId); // 需要通过 UserRepository 添加这个方法
    return roles.stream().anyMatch(role -> "ADMIN".equals(role.getCode()));
  }

  /** 获取当前用户 ID */
  private Long getCurrentUserId() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getId();
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
  }
}
