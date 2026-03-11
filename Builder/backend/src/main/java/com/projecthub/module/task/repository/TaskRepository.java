package com.projecthub.module.task.repository;

import com.projecthub.module.task.entity.Task;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 任务 Repository 接口 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

  /** 查询项目下的任务列表 */
  @Query(
      "SELECT t FROM Task t WHERE t.projectId = :projectId AND t.deletedAt IS NULL "
          + "ORDER BY t.position ASC")
  List<Task> findByProjectId(@Param("projectId") Long projectId);

  /** 查询项目下的任务列表（分页） */
  @Query(
      "SELECT t FROM Task t WHERE t.projectId = :projectId AND t.deletedAt IS NULL "
          + "ORDER BY t.createdAt DESC")
  Page<Task> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  /** 根据负责人查询任务 */
  @Query(
      "SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.deletedAt IS NULL "
          + "ORDER BY t.createdAt DESC")
  List<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId);

  /** 查询项目下指定状态的任务 */
  @Query(
      "SELECT t FROM Task t WHERE t.projectId = :projectId AND t.status = :status AND t.deletedAt IS NULL "
          + "ORDER BY t.position ASC")
  List<Task> findByProjectIdAndStatus(
      @Param("projectId") Long projectId, @Param("status") Task.TaskStatus status);

  /** 统计项目下的任务数量 */
  @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.deletedAt IS NULL")
  Long countByProjectId(@Param("projectId") Long projectId);

  /** 查询最大位置值 */
  @Query("SELECT COALESCE(MAX(t.position), -1) FROM Task t WHERE t.projectId = :projectId")
  Integer findMaxPosition(@Param("projectId") Long projectId);

  /** 查询父任务下的子任务列表 */
  @Query(
      "SELECT t FROM Task t WHERE t.parentId = :parentId AND t.deletedAt IS NULL ORDER BY t.position ASC")
  List<Task> findByParentId(@Param("parentId") Long parentId);

  /** 查询子任务数量 */
  @Query("SELECT COUNT(t) FROM Task t WHERE t.parentId = :parentId AND t.deletedAt IS NULL")
  Long countByParentId(@Param("parentId") Long parentId);
}
