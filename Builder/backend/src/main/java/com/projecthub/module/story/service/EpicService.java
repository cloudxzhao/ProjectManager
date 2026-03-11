package com.projecthub.module.story.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.story.dto.EpicVO;
import com.projecthub.module.story.entity.Epic;
import com.projecthub.module.story.repository.EpicRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 史诗服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpicService {

  private final EpicRepository epicRepository;
  private final PermissionService permissionService;

  /** 创建史诗 */
  @Transactional
  public EpicVO createEpic(Long projectId, EpicVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "EPIC_CREATE")) {
      throw new BusinessException(403, "无创建史诗权限");
    }

    // 获取最大位置
    Integer maxPosition = epicRepository.findMaxPosition(projectId);

    // 创建史诗
    Epic epic =
        Epic.builder()
            .projectId(projectId)
            .title(request.getTitle())
            .description(request.getDescription())
            .color(request.getColor())
            .position(maxPosition + 1)
            .build();

    epicRepository.save(epic);
    log.info("创建史诗成功：epicId={}, projectId={}", epic.getId(), projectId);

    return BeanCopyUtil.copyProperties(epic, EpicVO.class);
  }

  /** 获取史诗详情 */
  @Transactional(readOnly = true)
  public EpicVO getEpic(Long epicId) {
    Epic epic =
        epicRepository.findById(epicId).orElseThrow(() -> new BusinessException("史诗不存在"));

    return BeanCopyUtil.copyProperties(epic, EpicVO.class);
  }

  /** 获取项目下的史诗列表 */
  @Transactional(readOnly = true)
  public List<EpicVO> listEpics(Long projectId) {
    List<Epic> epics = epicRepository.findByProjectIdOrderByPositionAsc(projectId);

    return epics.stream()
        .map(epic -> BeanCopyUtil.copyProperties(epic, EpicVO.class))
        .collect(Collectors.toList());
  }

  /** 更新史诗 */
  @Transactional
  public EpicVO updateEpic(Long epicId, EpicVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    Epic epic = epicRepository.findById(epicId).orElseThrow(() -> new BusinessException("史诗不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, epic.getProjectId(), "EPIC_EDIT")) {
      throw new BusinessException(403, "无编辑史诗权限");
    }

    // 更新字段
    if (request.getTitle() != null) {
      epic.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      epic.setDescription(request.getDescription());
    }
    if (request.getColor() != null) {
      epic.setColor(request.getColor());
    }

    epicRepository.save(epic);
    log.info("更新史诗成功：epicId={}", epicId);

    return BeanCopyUtil.copyProperties(epic, EpicVO.class);
  }

  /** 删除史诗 */
  @Transactional
  public void deleteEpic(Long epicId) {
    Long userId = getCurrentUserId();

    Epic epic = epicRepository.findById(epicId).orElseThrow(() -> new BusinessException("史诗不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, epic.getProjectId(), "EPIC_DELETE")) {
      throw new BusinessException(403, "无删除史诗权限");
    }

    epicRepository.delete(epic);
    log.info("删除史诗成功：epicId={}", epicId);
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