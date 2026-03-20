package com.projecthub.module.wiki.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.repository.ProjectRepository;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.module.wiki.dto.*;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.entity.WikiHistory;
import com.projecthub.module.wiki.enums.WikiChangeType;
import com.projecthub.module.wiki.enums.WikiStatus;
import com.projecthub.module.wiki.repository.WikiHistoryRepository;
import com.projecthub.module.wiki.repository.WikiRepository;
import com.projecthub.module.wiki.util.WikiContentUtils;
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
  private final UserRepository userRepository;

  /** 创建文档 */
  @Transactional
  public WikiVO createDocument(Long projectId, WikiVO.CreateRequest request) {
    Long userId = getCurrentUserId();

    // 检查项目是否存在
    if (!projectRepository.existsById(projectId)) {
      throw new BusinessException(404, "项目不存在");
    }

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "WIKI_CREATE")) {
      throw new BusinessException(403, "无创建文档权限");
    }

    // 检查父文档是否存在
    Long parentId = request.getParentId();
    if (parentId != null) {
      WikiDocument parent =
          wikiRepository.findById(parentId).orElseThrow(() -> new BusinessException(404, "父文档不存在"));
      if (!parent.getProjectId().equals(projectId)) {
        throw new BusinessException(400, "父文档不属于当前项目");
      }
    }

    // 生成 parentPath 和 level
    String parentPath = generateParentPath(parentId);
    int level = WikiContentUtils.calculateLevel(parentPath);

    // 渲染 Markdown 为 HTML
    String contentHtml = WikiContentUtils.renderToHtml(request.getContent());
    String summary = WikiContentUtils.generateSummary(request.getContent());

    // 创建文档
    WikiDocument document =
        WikiDocument.builder()
            .projectId(projectId)
            .parentId(parentId)
            .title(request.getTitle())
            .content(request.getContent())
            .contentHtml(contentHtml)
            .summary(summary)
            .authorId(userId)
            .orderNum(0)
            .status(request.getStatus() != null ? request.getStatus() : WikiStatus.PUBLISHED)
            .parentPath(parentPath)
            .level(level)
            .viewCount(0)
            .build();

    wikiRepository.save(document);

    // 保存初始历史记录
    WikiHistory history =
        WikiHistory.builder()
            .documentId(document.getId())
            .userId(userId)
            .version(1)
            .content(document.getContent())
            .contentHtml(document.getContentHtml())
            .changeType(WikiChangeType.CREATE)
            .changeLog("创建文档")
            .build();
    wikiHistoryRepository.save(history);

    log.info("创建 Wiki 文档成功：documentId={}", document.getId());
    return convertToVO(document);
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
    WikiVO vo = convertToVO(document);

    // 检查是否有子文档
    vo.setHasChildren(wikiRepository.existsByParentId(document.getId()));

    // 获取子文档
    List<WikiDocument> children = wikiRepository.findByParentIdOrderByOrderNumAsc(document.getId());

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
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

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
            .contentHtml(document.getContentHtml())
            .changeType(WikiChangeType.UPDATE)
            .changeLog(request.getChangeLog())
            .version(document.getVersion() + 1)
            .build();
    wikiHistoryRepository.save(history);

    // 更新文档
    if (request.getTitle() != null) {
      document.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      document.setContent(request.getContent());
      // 重新渲染 HTML
      document.setContentHtml(WikiContentUtils.renderToHtml(request.getContent()));
      document.setSummary(WikiContentUtils.generateSummary(request.getContent()));
    }
    if (request.getStatus() != null) {
      document.setStatus(request.getStatus());
    }

    document.setVersion(document.getVersion() + 1);
    wikiRepository.save(document);

    log.info("更新 Wiki 文档成功：documentId={}", documentId);
    return convertToVO(document);
  }

  /** 获取文档详情 */
  @Transactional(readOnly = true)
  public WikiDetailVO getDocument(Long documentId) {
    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 增加浏览次数
    wikiRepository.incrementViewCount(documentId);

    // 获取作者名称
    String authorName = getUserName(document.getAuthorId());

    WikiDetailVO vo = convertToDetailVO(document);
    vo.setAuthorName(authorName);
    vo.setHasChildren(wikiRepository.existsByParentId(documentId));

    return vo;
  }

  /** 删除文档 */
  @Transactional
  public void deleteDocument(Long documentId) {
    Long userId = getCurrentUserId();

    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_DELETE")) {
      throw new BusinessException(403, "无删除文档权限");
    }

    // 检查是否有子文档
    if (wikiRepository.existsByParentId(documentId)) {
      throw new BusinessException(400, "该文档下存在子文档，无法删除");
    }

    wikiRepository.delete(document);
    log.info("删除 Wiki 文档成功：documentId={}", documentId);
  }

  /** 移动文档 */
  @Transactional
  public WikiVO moveDocument(Long documentId, WikiVO.MoveRequest request) {
    Long userId = getCurrentUserId();

    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_EDIT")) {
      throw new BusinessException(403, "无移动文档权限");
    }

    // 检查新的父文档是否存在（如果不为空）
    Long newParentId = request.getParentId();
    if (newParentId != null) {
      // 不能将文档移动到自身或其子文档
      if (newParentId.equals(documentId)) {
        throw new BusinessException(400, "不能将文档移动到自身");
      }

      WikiDocument newParent =
          wikiRepository
              .findById(newParentId)
              .orElseThrow(() -> new BusinessException(404, "父文档不存在"));

      if (!newParent.getProjectId().equals(document.getProjectId())) {
        throw new BusinessException(400, "父文档不属于当前项目");
      }
    }

    // 更新父ID和排序号
    if (newParentId != null) {
      document.setParentId(newParentId);
      document.setParentPath(generateParentPath(newParentId));
      document.setLevel(WikiContentUtils.calculateLevel(document.getParentPath()));
    } else {
      document.setParentId(null);
      document.setParentPath("/");
      document.setLevel(0);
    }

    if (request.getOrderNum() != null) {
      document.setOrderNum(request.getOrderNum());
    }

    wikiRepository.save(document);

    log.info(
        "移动 Wiki 文档成功：documentId={}, newParentId={}, newOrderNum={}",
        documentId,
        newParentId,
        request.getOrderNum());

    return convertToVO(document);
  }

  /** 全文搜索 */
  @Transactional(readOnly = true)
  public List<WikiSearchResultVO> searchDocuments(Long projectId, String keyword, Integer limit) {
    Long userId = getCurrentUserId();

    // 权限校验
    if (!permissionService.hasPermission(userId, projectId, "WIKI_VIEW")) {
      throw new BusinessException(403, "无查看文档权限");
    }

    if (limit == null || limit <= 0) {
      limit = 20;
    }

    List<WikiDocument> documents = wikiRepository.searchByKeyword(projectId, keyword, limit);

    return documents.stream()
        .map(
            doc -> {
              WikiSearchResultVO vo = new WikiSearchResultVO();
              vo.setId(doc.getId());
              vo.setProjectId(doc.getProjectId());
              vo.setTitle(doc.getTitle());
              vo.setSummary(doc.getSummary());
              vo.setStatus(doc.getStatus());
              vo.setAuthorId(doc.getAuthorId());
              vo.setAuthorName(getUserName(doc.getAuthorId()));
              vo.setViewCount(doc.getViewCount());
              vo.setCreatedAt(doc.getCreatedAt());
              vo.setUpdatedAt(doc.getUpdatedAt());

              // 简单的高亮处理
              String highlight = generateHighlight(doc.getContent(), keyword);
              vo.setHighlight(highlight);

              return vo;
            })
        .collect(Collectors.toList());
  }

  /** 检查是否有子文档 */
  @Transactional(readOnly = true)
  public Boolean hasChildren(Long documentId) {
    return wikiRepository.existsByParentId(documentId);
  }

  /** 获取文档历史记录 */
  @Transactional(readOnly = true)
  public List<WikiHistory> getDocumentHistory(Long documentId) {
    wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

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

  /** 生成父路径 */
  private String generateParentPath(Long parentId) {
    if (parentId == null) {
      return "/";
    }

    WikiDocument parent = wikiRepository.findById(parentId).orElse(null);
    if (parent == null) {
      return "/" + parentId + "/";
    }

    String parentPath = parent.getParentPath();
    if (parentPath == null || parentPath.isEmpty()) {
      parentPath = "/";
    }

    return parentPath + parentId + "/";
  }

  /** 获取用户名 */
  private String getUserName(Long userId) {
    return userRepository
        .findById(userId)
        .map(user -> user.getNickname() != null ? user.getNickname() : user.getUsername())
        .orElse("未知用户");
  }

  /** 生成搜索高亮 */
  private String generateHighlight(String content, String keyword) {
    if (content == null || keyword == null) {
      return "";
    }

    int index = content.toLowerCase().indexOf(keyword.toLowerCase());
    if (index == -1) {
      return WikiContentUtils.generateSummary(content);
    }

    int start = Math.max(0, index - 50);
    int end = Math.min(content.length(), index + keyword.length() + 50);

    StringBuilder sb = new StringBuilder();
    if (start > 0) {
      sb.append("...");
    }
    sb.append(content, start, end);
    if (end < content.length()) {
      sb.append("...");
    }

    return sb.toString();
  }

  /** 转换为 VO */
  private WikiVO convertToVO(WikiDocument document) {
    WikiVO vo = new WikiVO();
    vo.setId(document.getId());
    vo.setProjectId(document.getProjectId());
    vo.setParentId(document.getParentId());
    vo.setTitle(document.getTitle());
    vo.setContent(document.getContent());
    vo.setContentHtml(document.getContentHtml());
    vo.setSummary(document.getSummary());
    vo.setAuthorId(document.getAuthorId());
    vo.setVersion(document.getVersion());
    vo.setOrderNum(document.getOrderNum());
    vo.setStatus(document.getStatus());
    vo.setParentPath(document.getParentPath());
    vo.setLevel(document.getLevel());
    vo.setViewCount(document.getViewCount());
    vo.setCreatedAt(document.getCreatedAt());
    vo.setUpdatedAt(document.getUpdatedAt());
    return vo;
  }

  /** 转换为详情 VO */
  private WikiDetailVO convertToDetailVO(WikiDocument document) {
    WikiDetailVO vo = new WikiDetailVO();
    vo.setId(document.getId());
    vo.setProjectId(document.getProjectId());
    vo.setParentId(document.getParentId());
    vo.setTitle(document.getTitle());
    vo.setContent(document.getContent());
    vo.setContentHtml(document.getContentHtml());
    vo.setSummary(document.getSummary());
    vo.setAuthorId(document.getAuthorId());
    vo.setVersion(document.getVersion());
    vo.setOrderNum(document.getOrderNum());
    vo.setStatus(document.getStatus());
    vo.setParentPath(document.getParentPath());
    vo.setLevel(document.getLevel());
    vo.setViewCount(document.getViewCount());
    vo.setCreatedAt(document.getCreatedAt());
    vo.setUpdatedAt(document.getUpdatedAt());
    return vo;
  }
}
