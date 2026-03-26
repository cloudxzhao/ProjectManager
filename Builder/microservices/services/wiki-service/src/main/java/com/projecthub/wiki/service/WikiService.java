package com.projecthub.wiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projecthub.wiki.dto.*;
import com.projecthub.wiki.entity.WikiPage;
import com.projecthub.wiki.entity.WikiPageHistory;
import com.projecthub.wiki.entity.WikiSpace;
import com.projecthub.wiki.repository.WikiPageHistoryRepository;
import com.projecthub.wiki.repository.WikiPageRepository;
import com.projecthub.wiki.repository.WikiSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiSpaceRepository wikiSpaceRepository;
    private final WikiPageRepository wikiPageRepository;
    private final WikiPageHistoryRepository wikiPageHistoryRepository;

    // Space Operations

    @Transactional
    public SpaceVO createSpace(CreateSpaceRequest request) {
        WikiSpace space = new WikiSpace();
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setProjectId(request.getProjectId());
        space.setIcon(request.getIcon());
        space.setOwnerId(String.valueOf(getCurrentUserId()));

        wikiSpaceRepository.insert(space);
        return convertToSpaceVO(space);
    }

    public SpaceVO getSpaceById(Long id) {
        WikiSpace space = wikiSpaceRepository.selectById(id);
        if (space == null) {
            throw new RuntimeException("Wiki space not found: " + id);
        }
        return convertToSpaceVO(space);
    }

    public List<SpaceVO> getSpacesByProjectId(Long projectId) {
        List<WikiSpace> spaces = wikiSpaceRepository.findByProjectId(projectId);
        return spaces.stream()
                .map(this::convertToSpaceVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SpaceVO updateSpace(Long id, CreateSpaceRequest request) {
        WikiSpace space = wikiSpaceRepository.selectById(id);
        if (space == null) {
            throw new RuntimeException("Wiki space not found: " + id);
        }

        if (request.getName() != null) {
            space.setName(request.getName());
        }
        if (request.getDescription() != null) {
            space.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            space.setIcon(request.getIcon());
        }

        space.setUpdatedAt(LocalDateTime.now());
        wikiSpaceRepository.updateById(space);
        return convertToSpaceVO(space);
    }

    @Transactional
    public void deleteSpace(Long id) {
        wikiSpaceRepository.deleteById(id);
    }

    // Page Operations

    @Transactional
    public PageVO createPage(CreatePageRequest request) {
        WikiPage page = new WikiPage();
        page.setSpaceId(request.getSpaceId());
        page.setParentId(request.getParentId());
        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setSlug(request.getSlug());
        page.setOrderNum(request.getOrderNum() != null ? request.getOrderNum() : 0);
        page.setLevel(request.getParentId() != null ? 2 : 1);
        page.setCreatorId(getCurrentUserId());
        page.setLastEditorId(getCurrentUserId());

        wikiPageRepository.insert(page);
        return convertToPageVO(page);
    }

    public PageVO getPageById(Long id) {
        WikiPage page = wikiPageRepository.selectById(id);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + id);
        }
        return convertToPageVO(page);
    }

    public List<PageVO> getPagesBySpaceId(Long spaceId) {
        List<WikiPage> pages = wikiPageRepository.findBySpaceId(spaceId);
        return buildTree(pages);
    }

    public List<PageVO> getPagesByParentId(Long parentId) {
        List<WikiPage> pages = wikiPageRepository.findByParentId(parentId);
        return pages.stream()
                .map(this::convertToPageVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PageVO updatePage(Long id, UpdatePageRequest request) {
        WikiPage page = wikiPageRepository.selectById(id);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + id);
        }

        boolean contentChanged = false;
        if (request.getTitle() != null && !request.getTitle().equals(page.getTitle())) {
            page.setTitle(request.getTitle());
            contentChanged = true;
        }
        if (request.getContent() != null && !request.getContent().equals(page.getContent())) {
            contentChanged = true;
            page.setContent(request.getContent());
        }
        if (request.getSlug() != null && !request.getSlug().equals(page.getSlug())) {
            page.setSlug(request.getSlug());
        }
        if (request.getOrderNum() != null && !request.getOrderNum().equals(page.getOrderNum())) {
            page.setOrderNum(request.getOrderNum());
        }

        if (contentChanged) {
            // 保存版本历史
            savePageHistory(page, request.getChangeSummary());
        }

        page.setLastEditorId(getCurrentUserId());
        page.setUpdatedAt(LocalDateTime.now());
        wikiPageRepository.updateById(page);
        return convertToPageVO(page);
    }

    /**
     * 获取页面版本历史
     */
    public List<PageHistoryVO> getPageHistory(Long pageId) {
        WikiPage page = wikiPageRepository.selectById(pageId);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + pageId);
        }

        List<WikiPageHistory> histories = wikiPageHistoryRepository.findByPageId(pageId);
        return histories.stream()
                .map(this::convertToPageHistoryVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取页面指定版本
     */
    public PageHistoryVO getPageHistoryByVersion(Long pageId, Integer version) {
        WikiPage page = wikiPageRepository.selectById(pageId);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + pageId);
        }

        WikiPageHistory history = wikiPageHistoryRepository.findByPageIdAndVersion(pageId, version);
        if (history == null) {
            throw new RuntimeException("Version not found: " + version);
        }
        return convertToPageHistoryVO(history);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public PageVO rollbackToVersion(Long pageId, Integer version) {
        WikiPage page = wikiPageRepository.selectById(pageId);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + pageId);
        }

        WikiPageHistory targetHistory = wikiPageHistoryRepository.findByPageIdAndVersion(pageId, version);
        if (targetHistory == null) {
            throw new RuntimeException("Version not found: " + version);
        }

        // 更新当前页面
        page.setTitle(targetHistory.getTitle());
        page.setContent(targetHistory.getContent());
        page.setLastEditorId(getCurrentUserId());
        page.setUpdatedAt(LocalDateTime.now());
        wikiPageRepository.updateById(page);

        // 更新版本历史标记
        LambdaQueryWrapper<WikiPageHistory> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(WikiPageHistory::getPageId, pageId);
        WikiPageHistory currentHistory = wikiPageHistoryRepository.findCurrentByPageId(pageId);
        if (currentHistory != null) {
            currentHistory.setIsCurrent(false);
            wikiPageHistoryRepository.updateById(currentHistory);
        }

        targetHistory.setIsCurrent(true);
        wikiPageHistoryRepository.updateById(targetHistory);

        log.info("页面回滚到版本：pageId={}, version={}", pageId, version);
        return convertToPageVO(page);
    }

    /**
     * 保存页面版本历史
     */
    private void savePageHistory(WikiPage page, String changeSummary) {
        // 获取下一个版本号
        Integer nextVersion = wikiPageHistoryRepository.getMaxVersion(page.getId()) + 1;

        // 将当前版本标记为非当前
        WikiPageHistory currentHistory = wikiPageHistoryRepository.findCurrentByPageId(page.getId());
        if (currentHistory != null) {
            currentHistory.setIsCurrent(false);
            wikiPageHistoryRepository.updateById(currentHistory);
        }

        // 创建新版本记录
        WikiPageHistory history = new WikiPageHistory();
        history.setPageId(page.getId());
        history.setVersion(nextVersion);
        history.setTitle(page.getTitle());
        history.setContent(page.getContent());
        history.setChangeSummary(changeSummary);
        history.setEditorId(getCurrentUserId());
        history.setIsCurrent(true);

        wikiPageHistoryRepository.insert(history);
        log.info("保存页面版本历史：pageId={}, version={}", page.getId(), nextVersion);
    }

    @Transactional
    public void deletePage(Long id) {
        // 先获取页面信息
        WikiPage page = wikiPageRepository.selectById(id);
        if (page == null) {
            throw new RuntimeException("Wiki page not found: " + id);
        }
        wikiPageRepository.deleteById(id);
        log.info("删除 Wiki 页面：pageId={}, title={}", id, page.getTitle());
    }

    // Private helper methods

    private Long getCurrentUserId() {
        // TODO: Get from UserContextHolder
        return 1L;
    }

    private SpaceVO convertToSpaceVO(WikiSpace space) {
        SpaceVO vo = new SpaceVO();
        vo.setId(space.getId());
        vo.setName(space.getName());
        vo.setDescription(space.getDescription());
        vo.setProjectId(space.getProjectId());
        vo.setIcon(space.getIcon());
        vo.setOwnerId(space.getOwnerId());
        vo.setCreatedAt(space.getCreatedAt());
        vo.setUpdatedAt(space.getUpdatedAt());

        // Count pages
        LambdaQueryWrapper<WikiPage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WikiPage::getSpaceId, space.getId())
                .eq(WikiPage::getDeleted, 0);
        Long count = wikiPageRepository.selectCount(wrapper);
        vo.setPageCount(count.intValue());

        return vo;
    }

    private PageVO convertToPageVO(WikiPage page) {
        PageVO vo = new PageVO();
        vo.setId(page.getId());
        vo.setSpaceId(page.getSpaceId());
        vo.setParentId(page.getParentId());
        vo.setTitle(page.getTitle());
        vo.setContent(page.getContent());
        vo.setSlug(page.getSlug());
        vo.setOrderNum(page.getOrderNum());
        vo.setLevel(page.getLevel());
        vo.setCreatorId(page.getCreatorId());
        vo.setLastEditorId(page.getLastEditorId());
        vo.setCreatedAt(page.getCreatedAt());
        vo.setUpdatedAt(page.getUpdatedAt());
        return vo;
    }

    private PageHistoryVO convertToPageHistoryVO(WikiPageHistory history) {
        PageHistoryVO vo = new PageHistoryVO();
        vo.setId(history.getId());
        vo.setPageId(history.getPageId());
        vo.setVersion(history.getVersion());
        vo.setTitle(history.getTitle());
        vo.setContent(history.getContent());
        vo.setChangeSummary(history.getChangeSummary());
        vo.setEditorId(history.getEditorId());
        vo.setCreatedAt(history.getCreatedAt());
        vo.setIsCurrent(history.getIsCurrent());
        return vo;
    }

    private List<PageVO> buildTree(List<WikiPage> pages) {
        List<PageVO> pageVOs = pages.stream()
                .map(this::convertToPageVO)
                .collect(Collectors.toList());

        return pageVOs.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0)
                .peek(vo -> {
                    List<PageVO> children = pageVOs.stream()
                            .filter(child -> vo.getId().equals(child.getParentId()))
                            .collect(Collectors.toList());
                    vo.setChildren(children);
                })
                .collect(Collectors.toList());
    }
}