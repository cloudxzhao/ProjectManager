package com.projecthub.module.project.repository;

import com.projecthub.module.project.entity.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 项目成员 Repository 接口 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

  /** 根据项目 ID 和用户 ID 查询成员信息 */
  @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId")
  Optional<ProjectMember> findByProjectIdAndUserId(
      @Param("projectId") Long projectId, @Param("userId") Long userId);

  /** 查询项目的所有成员 */
  @Query(
      "SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId ORDER BY pm.joinedAt DESC")
  List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

  /** 查询用户参与的所有项目成员关系 */
  @Query("SELECT pm FROM ProjectMember pm WHERE pm.userId = :userId ORDER BY pm.projectId DESC")
  List<ProjectMember> findByUserId(@Param("userId") Long userId);

  /** 查询用户参与的项目 ID 列表 */
  @Query("SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId")
  List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

  /** 删除项目成员 */
  void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
