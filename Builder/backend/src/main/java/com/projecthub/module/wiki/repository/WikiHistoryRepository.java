package com.projecthub.module.wiki.repository;

import com.projecthub.module.wiki.entity.WikiHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Wiki 历史记录 Repository 接口 */
@Repository
public interface WikiHistoryRepository extends JpaRepository<WikiHistory, Long> {

  /** 查询文档的历史记录（按创建时间倒序） */
  @Query("SELECT h FROM WikiHistory h WHERE h.documentId = :documentId ORDER BY h.createdAt DESC")
  List<WikiHistory> findByDocumentIdOrderByCreatedAtDesc(@Param("documentId") Long documentId);

  /** 查询文档的最新版本 */
  @Query(
      "SELECT h FROM WikiHistory h WHERE h.documentId = :documentId ORDER BY h.version DESC LIMIT 1")
  Optional<WikiHistory> findLatestVersion(@Param("documentId") Long documentId);

  /** 查询文档的最大版本号 */
  @Query("SELECT MAX(h.version) FROM WikiHistory h WHERE h.documentId = :documentId")
  Integer findMaxVersion(@Param("documentId") Long documentId);

  /** 根据版本号查询历史记录 */
  @Query("SELECT h FROM WikiHistory h WHERE h.documentId = :documentId AND h.version = :version")
  Optional<WikiHistory> findByDocumentIdAndVersion(
      @Param("documentId") Long documentId, @Param("version") Integer version);
}
