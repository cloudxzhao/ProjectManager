package com.projecthub.module.story.repository;

import com.projecthub.module.story.entity.Epic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 史诗 Repository 接口 */
@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {

  /** 查询项目下的史诗列表（按位置排序） */
  @Query(
      "SELECT e FROM Epic e WHERE e.projectId = :projectId AND e.deletedAt IS NULL ORDER BY e.position ASC")
  List<Epic> findByProjectIdOrderByPositionAsc(@Param("projectId") Long projectId);

  /** 统计项目下的史诗数量 */
  @Query("SELECT COUNT(e) FROM Epic e WHERE e.projectId = :projectId AND e.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);

  /** 查询最大位置值 */
  @Query("SELECT COALESCE(MAX(e.position), -1) FROM Epic e WHERE e.projectId = :projectId")
  Integer findMaxPosition(@Param("projectId") Long projectId);
}
