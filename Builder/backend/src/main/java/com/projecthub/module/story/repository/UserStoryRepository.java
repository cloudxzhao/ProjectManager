package com.projecthub.module.story.repository;

import com.projecthub.module.story.entity.UserStory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 用户故事 Repository 接口 */
@Repository
public interface UserStoryRepository
    extends JpaRepository<UserStory, Long>, JpaSpecificationExecutor<UserStory> {

  /** 查询项目下的用户故事列表（按位置排序） */
  @Query(
      "SELECT us FROM UserStory us WHERE us.projectId = :projectId AND us.deletedAt IS NULL ORDER BY us.position ASC")
  List<UserStory> findByProjectIdOrderByPositionAsc(@Param("projectId") Long projectId);

  /** 查询史诗下的用户故事列表 */
  @Query(
      "SELECT us FROM UserStory us WHERE us.epicId = :epicId AND us.deletedAt IS NULL ORDER BY us.position ASC")
  List<UserStory> findByEpicIdOrderByPositionAsc(@Param("epicId") Long epicId);

  /** 查询项目下的用户故事（分页） */
  @Query(
      "SELECT us FROM UserStory us WHERE us.projectId = :projectId AND us.deletedAt IS NULL ORDER BY us.createdAt DESC")
  Page<UserStory> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  /** 统计项目下的用户故事数量 */
  @Query(
      "SELECT COUNT(us) FROM UserStory us WHERE us.projectId = :projectId AND us.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);

  /** 统计史诗下的用户故事数量 */
  @Query("SELECT COUNT(us) FROM UserStory us WHERE us.epicId = :epicId AND us.deletedAt IS NULL")
  Long countByEpicId(@Param("epicId") Long epicId);

  /** 查询最大位置值 */
  @Query("SELECT COALESCE(MAX(us.position), -1) FROM UserStory us WHERE us.projectId = :projectId")
  Integer findMaxPosition(@Param("projectId") Long projectId);
}