package com.projecthub.module.issue.repository;

import com.projecthub.common.constant.TaskStatus;
import com.projecthub.module.issue.entity.Issue;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 问题 Repository 接口 */
@Repository
public interface IssueRepository
    extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

  /** 查询项目下的问题列表 */
  @Query(
      "SELECT i FROM Issue i WHERE i.projectId = :projectId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
  List<Issue> findByProjectId(@Param("projectId") Long projectId);

  /** 查询项目下的问题（分页） */
  @Query(
      "SELECT i FROM Issue i WHERE i.projectId = :projectId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
  Page<Issue> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  /** 查询项目下指定状态的问题 */
  @Query(
      "SELECT i FROM Issue i WHERE i.projectId = :projectId AND i.status = :status AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
  List<Issue> findByProjectIdAndStatus(
      @Param("projectId") Long projectId, @Param("status") TaskStatus status);

  /** 统计项目下的问题数量 */
  @Query("SELECT COUNT(i) FROM Issue i WHERE i.projectId = :projectId AND i.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);

  /** 统计项目下指定状态的问题数量 */
  @Query(
      "SELECT COUNT(i) FROM Issue i WHERE i.projectId = :projectId AND i.status = :status AND i.deletedAt IS NULL")
  Long countByProjectIdAndStatus(
      @Param("projectId") Long projectId, @Param("status") TaskStatus status);
}
