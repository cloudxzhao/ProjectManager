package com.projecthub.module.wiki.repository;

import com.projecthub.module.wiki.entity.WikiDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Wiki 文档 Repository 接口 */
@Repository
public interface WikiRepository extends JpaRepository<WikiDocument, Long> {

  /** 查询项目下的根文档（按层级和排序号） */
  @Query(
      "SELECT w FROM WikiDocument w WHERE w.projectId = :projectId AND w.parentId IS NULL AND w.deletedAt IS NULL ORDER BY w.orderNum ASC")
  List<WikiDocument> findRootDocumentsByProjectId(@Param("projectId") Long projectId);

  /** 查询父文档下的子文档 */
  @Query(
      "SELECT w FROM WikiDocument w WHERE w.parentId = :parentId AND w.deletedAt IS NULL ORDER BY w.orderNum ASC")
  List<WikiDocument> findByParentIdOrderByOrderNumAsc(@Param("parentId") Long parentId);

  /** 统计项目下的文档数量 */
  @Query(
      "SELECT COUNT(w) FROM WikiDocument w WHERE w.projectId = :projectId AND w.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);

  /** 全文搜索 */
  @Query(
      value =
          "SELECT * FROM wiki_document w WHERE w.project_id = :projectId AND w.search_vector @@ plainto_tsquery('simple', :keyword) AND w.deleted_at IS NULL ORDER BY ts_rank(w.search_vector, plainto_tsquery('simple', :keyword)) DESC LIMIT :limit",
      nativeQuery = true)
  List<WikiDocument> searchByKeyword(
      @Param("projectId") Long projectId,
      @Param("keyword") String keyword,
      @Param("limit") Integer limit);

  /** 检查是否有子文档 */
  @Query(
      "SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WikiDocument w WHERE w.parentId = :parentId AND w.deletedAt IS NULL")
  Boolean existsByParentId(@Param("parentId") Long parentId);

  /** 更新父路径和层级 */
  @Modifying
  @Query("UPDATE WikiDocument w SET w.parentPath = :parentPath, w.level = :level WHERE w.id = :id")
  void updateParentPathAndLevel(
      @Param("id") Long id, @Param("parentPath") String parentPath, @Param("level") Integer level);

  /** 更新排序号 */
  @Modifying
  @Query("UPDATE WikiDocument w SET w.orderNum = :orderNum WHERE w.id = :id")
  void updateOrderNum(@Param("id") Long id, @Param("orderNum") Integer orderNum);

  /** 更新父ID */
  @Modifying
  @Query("UPDATE WikiDocument w SET w.parentId = :parentId WHERE w.id = :id")
  void updateParentId(@Param("id") Long id, @Param("parentId") Long parentId);

  /** 增加浏览次数 */
  @Modifying
  @Query("UPDATE WikiDocument w SET w.viewCount = w.viewCount + 1 WHERE w.id = :id")
  void incrementViewCount(@Param("id") Long id);
}
