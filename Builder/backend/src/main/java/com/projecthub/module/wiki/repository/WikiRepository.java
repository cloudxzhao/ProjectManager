package com.projecthub.module.wiki.repository;

import com.projecthub.module.wiki.entity.WikiDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Wiki 文档 Repository 接口 */
@Repository
public interface WikiRepository extends JpaRepository<WikiDocument, Long> {

  /** 查询项目下的文档列表（按位置排序） */
  @Query(
      "SELECT w FROM WikiDocument w WHERE w.projectId = :projectId AND w.parentId IS NULL AND w.deletedAt IS NULL ORDER BY w.position ASC")
  List<WikiDocument> findRootDocumentsByProjectId(@Param("projectId") Long projectId);

  /** 查询父文档下的子文档 */
  @Query(
      "SELECT w FROM WikiDocument w WHERE w.parentId = :parentId AND w.deletedAt IS NULL ORDER BY w.position ASC")
  List<WikiDocument> findByParentIdOrderByPositionAsc(@Param("parentId") Long parentId);

  /** 统计项目下的文档数量 */
  @Query("SELECT COUNT(w) FROM WikiDocument w WHERE w.projectId = :projectId AND w.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);
}