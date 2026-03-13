package com.projecthub.module.wiki.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.common.util.BeanCopyUtil;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.wiki.dto.WikiVO;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.entity.WikiHistory;
import com.projecthub.module.wiki.repository.WikiHistoryRepository;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.security.UserDetailsImpl;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Wiki 服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiService {

  private final WikiRepository wikiRepository;
  private final WikiHistoryRepository wikiHistoryRepository;
  private final PermissionService permissionService;
  private final ProjectRepository projectRepository;

  /** 创建文档 */
  @Transactional
  public WikiVO createDocument(Long projectId, WikiVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 检查项目是否存在
    if (!projectRepository.existsById(projectId)) {
      throw new BusinessException(404, 404, "项目不存在");
    }

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "WIKI_CREATE")) {
      throw new BusinessException(403, "无创建文档权限");
    }

    // 检查父文档是否存在
    if (request.getParentId() != null) {
      wikiRepository
          .findById(request.getParentId())
          .orElseThrow(() -> new BusinessException(404, 404, "父文档不存在"));
    }

    // 创建文档
    WikiDocument document =
        WikiDocument.builder()
            .projectId(projectId)
            .parentId(request.getParentId())
            .title(request.getTitle())
            .content(request.getContent())
            .position(0)
            .build();

    wikiRepository.save(document);

    // 保存历史记录
    WikiHistory history =
        WikiHistory.builder()
            .documentId(document.getId())
            .userId(userId)
            .content(document.getContent())
            .build();
    wikiHistoryRepository.save(history);

    log.info("创建 Wiki 文档成功：documentId={}", document.getId());
    return BeanCopyUtil.copyProperties(document, WikiVO.class);
  }

  /** 获取项目下的文档树 */
  @Transactional(readOnly = true)
  public List<WikiVO> getDocumentTree(Long projectId) {
    // 权限校验
    Long userId = getCurrentUserId();
    if (!permissionService.hasPermission(userId, projectId, "WIKI_VIEW")) {
      throw new BusinessException(403, "无查看文档权限");
    }

    // 获取根文档
    List<WikiDocument> rootDocuments = wikiRepository.findRootDocumentsByProjectId(projectId);

    // 构建树形结构
    return rootDocuments.stream().map(this::buildDocumentTree).collect(Collectors.toList());
  }

  /** 构建文档树 */
  private WikiVO buildDocumentTree(WikiDocument document) {
    WikiVO vo = BeanCopyUtil.copyProperties(document, WikiVO.class);

    // 获取子文档
    List<WikiDocument> children = wikiRepository.findByParentIdOrderByPositionAsc(document.getId());

    if (children != null && !children.isEmpty()) {
      vo.setChildren(children.stream().map(this::buildDocumentTree).collect(Collectors.toList()));
    }

    return vo;
  }

  /** 更新文档 */
  @Transactional
  public WikiVO updateDocument(Long documentId, WikiVO.UpdateRequest request) {
    Long userId = getCurrentUserId();

    WikiDocument document =
        wikiRepository
            .findById(documentId)
            .orElseThrow(() -> new BusinessException(404, 404, "文档不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_EDIT")) {
      throw new BusinessException(403, "无编辑文档权限");
    }

    // 保存旧版本到历史记录
    WikiHistory history =
        WikiHistory.builder()
            .documentId(documentId)
            .userId(userId)
            .content(document.getContent())
            .build();
    wikiHistoryRepository.save(history);

    // 更新文档
    if (request.getTitle() != null) {
      document.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      document.setContent(request.getContent());
    }

    wikiRepository.save(document);

    log.info("更新 Wiki 文档成功：documentId={}", documentId);
    return BeanCopyUtil.copyProperties(document, WikiVO.class);
  }

  /** 获取文档详情 */
  @Transactional(readOnly = true)
  public WikiVO getDocument(Long documentId) {
    WikiDocument document =
        wikiRepository
            .findById(documentId)
            .orElseThrow(() -> new BusinessException(404, 404, "文档不存在"));

    return BeanCopyUtil.copyProperties(document, WikiVO.class);
  }

  /** 删除文档 */
  @Transactional
  public void deleteDocument(Long documentId) {
    Long userId = getCurrentUserId();

    WikiDocument document =
        wikiRepository
            .findById(documentId)
            .orElseThrow(() -> new BusinessException(404, 404, "文档不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_DELETE")) {
      throw new BusinessException(403, "无删除文档权限");
    }

    wikiRepository.delete(document);
    log.info("删除 Wiki 文档成功：documentId={}", documentId);
  }

  /** 获取文档历史记录 */
  @Transactional(readOnly = true)
  public List<WikiHistory> getDocumentHistory(Long documentId) {
    wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, 404, "文档不存在"));

    return wikiHistoryRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
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
