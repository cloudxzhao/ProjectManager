package com.projecthub.story.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projecthub.common.api.result.Result;
import com.projecthub.story.dto.*;
import com.projecthub.story.entity.Epic;
import com.projecthub.story.entity.UserStory;
import com.projecthub.story.feign.ProjectServiceClient;
import com.projecthub.story.feign.dto.ProjectInfoDTO;
import com.projecthub.story.repository.EpicRepository;
import com.projecthub.story.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final EpicRepository epicRepository;
    private final UserStoryRepository userStoryRepository;
    private final ProjectServiceClient projectServiceClient;

    // Epic Operations

    @Transactional
    public EpicVO createEpic(CreateEpicRequest request) {
        Epic epic = new Epic();
        epic.setName(request.getName());
        epic.setDescription(request.getDescription());
        epic.setProjectId(request.getProjectId());
        epic.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        epic.setStatus("OPEN");
        epic.setCreatorId(getCurrentUserId());

        epicRepository.insert(epic);
        return convertToEpicVO(epic);
    }

    public EpicVO getEpicById(Long id) {
        Epic epic = epicRepository.selectById(id);
        if (epic == null) {
            throw new RuntimeException("Epic not found: " + id);
        }
        return convertToEpicVO(epic);
    }

    public List<EpicVO> getEpicsByProjectId(Long projectId) {
        List<Epic> epics = epicRepository.findByProjectId(projectId);
        return epics.stream()
                .map(this::convertToEpicVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EpicVO updateEpic(Long id, CreateEpicRequest request) {
        Epic epic = epicRepository.selectById(id);
        if (epic == null) {
            throw new RuntimeException("Epic not found: " + id);
        }

        if (request.getName() != null) {
            epic.setName(request.getName());
        }
        if (request.getDescription() != null) {
            epic.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            epic.setPriority(request.getPriority());
        }

        epic.setUpdatedAt(LocalDateTime.now());
        epicRepository.updateById(epic);
        return convertToEpicVO(epic);
    }

    @Transactional
    public void deleteEpic(Long id) {
        epicRepository.deleteById(id);
    }

    /**
     * Get project info from project service via Feign
     */
    public ProjectInfoDTO getProjectInfo(Long projectId) {
        try {
            Result<ProjectInfoDTO> result = projectServiceClient.getProjectById(projectId);
            if (result != null && result.getCode() != null && result.getCode() == 200) {
                return result.getData();
            }
            log.warn("Failed to get project info from project-service, projectId: {}, result: {}", projectId, result);
        } catch (Exception e) {
            log.error("Error calling project-service for projectId: {}", projectId, e);
        }
        return null;
    }

    /**
     * Check if project exists
     */
    public boolean isProjectValid(Long projectId) {
        return getProjectInfo(projectId) != null;
    }

    // User Story Operations

    @Transactional
    public StoryVO createStory(CreateStoryRequest request) {
        UserStory story = new UserStory();
        story.setTitle(request.getTitle());
        story.setDescription(request.getDescription());
        story.setAcceptanceCriteria(request.getAcceptanceCriteria());
        story.setEpicId(request.getEpicId());
        story.setProjectId(request.getProjectId());
        story.setAssigneeId(request.getAssigneeId());
        story.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        story.setStoryPoints(request.getStoryPoints());
        story.setStoryKey(generateStoryKey(request.getProjectId()));
        story.setStatus("OPEN");
        story.setCreatorId(getCurrentUserId());

        userStoryRepository.insert(story);
        return convertToStoryVO(story);
    }

    public StoryVO getStoryById(Long id) {
        UserStory story = userStoryRepository.selectById(id);
        if (story == null) {
            throw new RuntimeException("User story not found: " + id);
        }
        return convertToStoryVO(story);
    }

    public List<StoryVO> getStoriesByProjectId(Long projectId) {
        List<UserStory> stories = userStoryRepository.findByProjectId(projectId);
        return stories.stream()
                .map(this::convertToStoryVO)
                .collect(Collectors.toList());
    }

    public List<StoryVO> getStoriesByEpicId(Long epicId) {
        LambdaQueryWrapper<UserStory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStory::getEpicId, epicId)
                .eq(UserStory::getDeleted, 0)
                .orderByDesc(UserStory::getCreatedAt);
        List<UserStory> stories = userStoryRepository.selectList(wrapper);
        return stories.stream()
                .map(this::convertToStoryVO)
                .collect(Collectors.toList());
    }

    public IPage<StoryVO> getStoriesPage(Long projectId, int pageNum, int pageSize) {
        Page<UserStory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserStory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStory::getProjectId, projectId)
                .eq(UserStory::getDeleted, 0)
                .orderByDesc(UserStory::getCreatedAt);

        IPage<UserStory> storyPage = userStoryRepository.selectPage(page, wrapper);
        return storyPage.convert(this::convertToStoryVO);
    }

    @Transactional
    public StoryVO updateStory(Long id, CreateStoryRequest request) {
        UserStory story = userStoryRepository.selectById(id);
        if (story == null) {
            throw new RuntimeException("User story not found: " + id);
        }

        if (request.getTitle() != null) {
            story.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            story.setDescription(request.getDescription());
        }
        if (request.getAcceptanceCriteria() != null) {
            story.setAcceptanceCriteria(request.getAcceptanceCriteria());
        }
        if (request.getEpicId() != null) {
            story.setEpicId(request.getEpicId());
        }
        if (request.getAssigneeId() != null) {
            story.setAssigneeId(request.getAssigneeId());
        }
        if (request.getPriority() != null) {
            story.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            story.setStoryPoints(request.getStoryPoints());
        }

        story.setUpdatedAt(LocalDateTime.now());
        userStoryRepository.updateById(story);
        return convertToStoryVO(story);
    }

    @Transactional
    public StoryVO updateStoryStatus(Long id, String status) {
        UserStory story = userStoryRepository.selectById(id);
        if (story == null) {
            throw new RuntimeException("User story not found: " + id);
        }

        story.setStatus(status);
        story.setUpdatedAt(LocalDateTime.now());
        userStoryRepository.updateById(story);
        return convertToStoryVO(story);
    }

    @Transactional
    public void deleteStory(Long id) {
        userStoryRepository.deleteById(id);
    }

    // Story Points Statistics

    /**
     * Get story points statistics by project
     */
    public StoryPointsStatsVO getStoryPointsStats(Long projectId) {
        StoryPointsStatsVO stats = new StoryPointsStatsVO();
        stats.setProjectId(projectId);

        // Get all stories for the project
        List<UserStory> allStories = userStoryRepository.findByProjectId(projectId);

        // Calculate overall statistics
        int totalPoints = 0;
        int completedPoints = 0;
        int inProgressPoints = 0;
        int notStartedPoints = 0;

        for (UserStory story : allStories) {
            Integer points = story.getStoryPoints();
            if (points != null && points > 0) {
                totalPoints += points;

                String status = story.getStatus();
                if ("DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                    completedPoints += points;
                } else if ("IN_PROGRESS".equalsIgnoreCase(status) || "INPROGRESS".equalsIgnoreCase(status)) {
                    inProgressPoints += points;
                } else {
                    notStartedPoints += points;
                }
            }
        }

        stats.setTotalPoints(totalPoints);
        stats.setCompletedPoints(completedPoints);
        stats.setInProgressPoints(inProgressPoints);
        stats.setNotStartedPoints(notStartedPoints);
        stats.setCompletionRate(totalPoints > 0 ? (double) completedPoints / totalPoints * 100 : 0.0);

        // Calculate epic-level statistics
        List<Epic> epics = epicRepository.findByProjectId(projectId);
        List<EpicStoryPointsVO> epicStatsList = new ArrayList<>();

        for (Epic epic : epics) {
            EpicStoryPointsVO epicStats = new EpicStoryPointsVO();
            epicStats.setEpicId(epic.getId());
            epicStats.setEpicName(epic.getName());

            LambdaQueryWrapper<UserStory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserStory::getEpicId, epic.getId())
                    .eq(UserStory::getDeleted, 0);
            List<UserStory> epicStories = userStoryRepository.selectList(wrapper);

            int epicTotalPoints = 0;
            int epicCompletedPoints = 0;

            for (UserStory story : epicStories) {
                Integer points = story.getStoryPoints();
                if (points != null && points > 0) {
                    epicTotalPoints += points;
                    String status = story.getStatus();
                    if ("DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        epicCompletedPoints += points;
                    }
                }
            }

            epicStats.setTotalPoints(epicTotalPoints);
            epicStats.setCompletedPoints(epicCompletedPoints);
            epicStats.setCompletionRate(epicTotalPoints > 0 ? (double) epicCompletedPoints / epicTotalPoints * 100 : 0.0);
            epicStatsList.add(epicStats);
        }

        stats.setEpicStats(epicStatsList);
        return stats;
    }

    // Private helper methods

    private String generateStoryKey(Long projectId) {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("STORY-%s-%s", projectId, uuid);
    }

    private Long getCurrentUserId() {
        // TODO: Get from UserContextHolder
        return 1L;
    }

    private EpicVO convertToEpicVO(Epic epic) {
        EpicVO vo = new EpicVO();
        vo.setId(epic.getId());
        vo.setName(epic.getName());
        vo.setDescription(epic.getDescription());
        vo.setProjectId(epic.getProjectId());
        vo.setStatus(epic.getStatus());
        vo.setPriority(epic.getPriority());
        vo.setStartDate(epic.getStartDate());
        vo.setEndDate(epic.getEndDate());
        vo.setCreatorId(epic.getCreatorId());
        vo.setCreatedAt(epic.getCreatedAt());
        vo.setUpdatedAt(epic.getUpdatedAt());

        // Count stories in this epic
        LambdaQueryWrapper<UserStory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStory::getEpicId, epic.getId())
                .eq(UserStory::getDeleted, 0);
        Long count = userStoryRepository.selectCount(wrapper);
        vo.setStoryCount(count.intValue());

        return vo;
    }

    private StoryVO convertToStoryVO(UserStory story) {
        StoryVO vo = new StoryVO();
        vo.setId(story.getId());
        vo.setStoryKey(story.getStoryKey());
        vo.setTitle(story.getTitle());
        vo.setDescription(story.getDescription());
        vo.setAcceptanceCriteria(story.getAcceptanceCriteria());
        vo.setEpicId(story.getEpicId());
        vo.setProjectId(story.getProjectId());
        vo.setAssigneeId(story.getAssigneeId());
        vo.setStatus(story.getStatus());
        vo.setPriority(story.getPriority());
        vo.setStoryPoints(story.getStoryPoints());
        vo.setCreatorId(story.getCreatorId());
        vo.setCreatedAt(story.getCreatedAt());
        vo.setUpdatedAt(story.getUpdatedAt());
        return vo;
    }
}