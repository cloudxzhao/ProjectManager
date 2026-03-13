package com.projecthub.module.project.repository;

import com.projecthub.module.project.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 项目 Repository 接口 */
@Repository
public interface ProjectRepository
    extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

  /** 查询用户参与的项目（作为成员） */
  @Query(
      "SELECT p FROM Project p JOIN ProjectMember pm ON p.id = pm.projectId "
          + "WHERE pm.userId = :userId AND p.deletedAt IS NULL "
          + "ORDER BY p.createdAt DESC")
  List<Project> findUserProjects(@Param("userId") Long userId);

  /** 查询用户拥有的项目 */
  @Query(
      "SELECT p FROM Project p WHERE p.ownerId = :ownerId AND p.deletedAt IS NULL "
          + "ORDER BY p.createdAt DESC")
  List<Project> findOwnerProjects(@Param("ownerId") Long ownerId);

  /** 检查项目是否存在且属于指定所有者 */
  boolean existsByIdAndOwnerId(Long id, Long ownerId);

  /** 查询项目 ID 列表（根据所有者 ID） */
  @Query("SELECT p.id FROM Project p WHERE p.ownerId = :ownerId AND p.deletedAt IS NULL")
  List<Long> findIdsByOwnerId(@Param("ownerId") Long ownerId);

  /** 统计项目成员数量 */
  @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.projectId = :projectId")
  Long countMembersByProjectId(@Param("projectId") Long projectId);

  /** 统计项目任务数量 */
  @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.deletedAt IS NULL")
  Long countTasksByProjectId(@Param("projectId") Long projectId);

  /** 统计项目已完成任务数量 */
  @Query(
      "SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId "
          + "AND t.status = com.projecthub.module.task.entity.Task$TaskStatus.DONE "
          + "AND t.deletedAt IS NULL")
  Long countCompletedTasksByProjectId(@Param("projectId") Long projectId);

  /** 统计用户各项目状态的项目数量 */
  @Query(
      "SELECT p.status, COUNT(p) FROM Project p WHERE p.id IN :projectIds "
          + "AND p.deletedAt IS NULL GROUP BY p.status")
  List<Object[]> countProjectsByStatus(@Param("projectIds") List<Long> projectIds);
}
