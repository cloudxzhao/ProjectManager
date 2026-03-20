package com.projecthub.module.wiki.service;

import com.projecthub.common.exception.BusinessException;
import com.projecthub.module.project.service.PermissionService;
import com.projecthub.module.user.repository.UserRepository;
import com.projecthub.module.wiki.dto.VersionDiffVO;
import com.projecthub.module.wiki.dto.WikiVersionVO;
import com.projecthub.module.wiki.entity.WikiDocument;
import com.projecthub.module.wiki.entity.WikiHistory;
import com.projecthub.module.wiki.enums.WikiChangeType;
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

/** Wiki 版本服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiVersionService {

  private final WikiRepository wikiRepository;
  private final WikiHistoryRepository wikiHistoryRepository;
  private final PermissionService permissionService;
  private final UserRepository userRepository;

  /** 获取版本列表 */
  @Transactional(readOnly = true)
  public List<WikiVersionVO> getVersionList(Long documentId) {
    // 检查文档是否存在
    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 权限校验
    Long userId = getCurrentUserId();
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_VIEW")) {
      throw new BusinessException(403, "无查看权限");
    }

    // 获取历史记录
    List<WikiHistory> histories =
        wikiHistoryRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);

    // 添加当前版本
    WikiVersionVO currentVersion = new WikiVersionVO();
    currentVersion.setId(null); // 表示当前版本
    currentVersion.setDocumentId(documentId);
    currentVersion.setVersion(document.getVersion());
    currentVersion.setContent(document.getContent());
    currentVersion.setContentHtml(document.getContentHtml());
    currentVersion.setChangeType(WikiChangeType.UPDATE);
    currentVersion.setChangeLog("当前版本");
    currentVersion.setUserId(document.getAuthorId());
    currentVersion.setUserName(getUserName(document.getAuthorId()));
    currentVersion.setCreatedAt(document.getUpdatedAt());

    List<WikiVersionVO> result =
        histories.stream()
            .map(
                h -> {
                  WikiVersionVO vo = new WikiVersionVO();
                  vo.setId(h.getId());
                  vo.setDocumentId(h.getDocumentId());
                  vo.setVersion(h.getVersion());
                  vo.setContent(h.getContent());
                  vo.setContentHtml(h.getContentHtml());
                  vo.setChangeLog(h.getChangeLog());
                  vo.setChangeType(h.getChangeType());
                  vo.setUserId(h.getUserId());
                  vo.setUserName(getUserName(h.getUserId()));
                  vo.setCreatedAt(h.getCreatedAt());
                  return vo;
                })
            .collect(Collectors.toList());

    // 将当前版本添加到列表开头
    result.add(0, currentVersion);

    return result;
  }

  /** 版本对比 */
  @Transactional(readOnly = true)
  public VersionDiffVO diffVersions(Long documentId, Long versionId, Long compareVersionId) {
    // 检查文档是否存在
    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 权限校验
    Long userId = getCurrentUserId();
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_VIEW")) {
      throw new BusinessException(403, "无查看权限");
    }

    // 获取旧版本内容
    String oldContent;
    Integer oldVersion;

    if (compareVersionId == null || compareVersionId == 0) {
      // 对比第一个版本
      List<WikiHistory> histories =
          wikiHistoryRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
      if (histories.isEmpty()) {
        oldContent = "";
        oldVersion = 1;
      } else {
        WikiHistory firstHistory = histories.get(histories.size() - 1);
        oldContent = firstHistory.getContent();
        oldVersion = firstHistory.getVersion();
      }
    } else if (compareVersionId == -1) {
      // 对比前一个版本
      WikiHistory currentHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, document.getVersion() - 1)
              .orElse(null);
      if (currentHistory == null) {
        List<WikiHistory> histories =
            wikiHistoryRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
        if (histories.isEmpty()) {
          oldContent = "";
          oldVersion = 1;
        } else {
          WikiHistory firstHistory = histories.get(histories.size() - 1);
          oldContent = firstHistory.getContent();
          oldVersion = firstHistory.getVersion();
        }
      } else {
        oldContent = currentHistory.getContent();
        oldVersion = currentHistory.getVersion();
      }
    } else if (versionId == null || versionId == 0 || versionId == -1) {
      // 对比当前版本和指定版本
      WikiHistory targetHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, Math.toIntExact(compareVersionId))
              .orElseThrow(() -> new BusinessException(404, "版本不存在"));
      oldContent = targetHistory.getContent();
      oldVersion = targetHistory.getVersion();
    } else {
      // 对比两个历史版本
      WikiHistory oldHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, Math.toIntExact(versionId))
              .orElseThrow(() -> new BusinessException(404, "旧版本不存在"));
      WikiHistory newHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, Math.toIntExact(compareVersionId))
              .orElseThrow(() -> new BusinessException(404, "新版本不存在"));
      oldContent = oldHistory.getContent();
      oldVersion = oldHistory.getVersion();
    }

    // 获取新版本内容
    String newContent;
    Integer newVersion;

    if (versionId == null || versionId == 0) {
      // 对比当前版本
      newContent = document.getContent();
      newVersion = document.getVersion();
    } else if (versionId == -1) {
      // 对比前一个版本
      WikiHistory currentHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, document.getVersion() - 1)
              .orElse(null);
      if (currentHistory == null) {
        newContent = document.getContent();
        newVersion = document.getVersion();
      } else {
        newContent = currentHistory.getContent();
        newVersion = currentHistory.getVersion();
      }
    } else {
      WikiHistory newHistory =
          wikiHistoryRepository
              .findByDocumentIdAndVersion(documentId, Math.toIntExact(versionId))
              .orElseThrow(() -> new BusinessException(404, "新版本不存在"));
      newContent = newHistory.getContent();
      newVersion = newHistory.getVersion();
    }

    // 构建返回结果
    VersionDiffVO vo = new VersionDiffVO();
    vo.setDocumentId(documentId);
    vo.setTitle(document.getTitle());
    vo.setOldVersion(oldVersion);
    vo.setNewVersion(newVersion);
    vo.setOldContent(oldContent);
    vo.setNewContent(newContent);
    vo.setDiffLines(WikiContentUtils.computeLineDiff(oldContent, newContent));
    vo.setDiffHtml(WikiContentUtils.diffToHtml(oldContent, newContent));

    return VersionDiffVO.calculateStats(vo);
  }

  /** 恢复版本 */
  @Transactional
  public WikiVersionVO restoreVersion(Long documentId, Long versionId, String changeLog) {
    Long userId = getCurrentUserId();

    // 检查文档是否存在
    WikiDocument document =
        wikiRepository.findById(documentId).orElseThrow(() -> new BusinessException(404, "文档不存在"));

    // 权限校验
    if (!permissionService.hasPermission(userId, document.getProjectId(), "WIKI_EDIT")) {
      throw new BusinessException(403, "无恢复权限");
    }

    // 获取要恢复的版本
    WikiHistory restoreHistory =
        wikiHistoryRepository
            .findByDocumentIdAndVersion(documentId, Math.toIntExact(versionId))
            .orElseThrow(() -> new BusinessException(404, "版本不存在"));

    // 保存当前版本到历史记录
    WikiHistory history =
        WikiHistory.builder()
            .documentId(documentId)
            .userId(userId)
            .content(document.getContent())
            .contentHtml(document.getContentHtml())
            .changeType(WikiChangeType.RESTORE)
            .changeLog(changeLog)
            .version(document.getVersion() + 1)
            .build();
    wikiHistoryRepository.save(history);

    // 恢复内容
    document.setContent(restoreHistory.getContent());
    document.setContentHtml(WikiContentUtils.renderToHtml(restoreHistory.getContent()));
    document.setSummary(WikiContentUtils.generateSummary(restoreHistory.getContent()));
    document.setVersion(document.getVersion() + 1);

    wikiRepository.save(document);

    log.info("恢复 Wiki 文档版本成功：documentId={}, 恢复到版本={}", documentId, versionId);

    WikiVersionVO vo = new WikiVersionVO();
    vo.setVersion(document.getVersion());
    vo.setContent(document.getContent());
    vo.setContentHtml(document.getContentHtml());
    vo.setChangeType(WikiChangeType.RESTORE);
    vo.setChangeLog(changeLog);
    vo.setUserId(userId);
    vo.setUserName(getUserName(userId));

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

  /** 获取用户名 */
  private String getUserName(Long userId) {
    return userRepository
        .findById(userId)
        .map(user -> user.getNickname() != null ? user.getNickname() : user.getUsername())
        .orElse("未知用户");
  }
}
