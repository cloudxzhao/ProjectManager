package com.projecthub.module.project.repository;

import com.projecthub.module.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目 Repository 接口
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    /**
     * 查询用户参与的项目（作为成员）
     */
    @Query("SELECT p FROM Project p JOIN ProjectMember pm ON p.id = pm.projectId " +
            "WHERE pm.userId = :userId AND p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    List<Project> findUserProjects(@Param("userId") Long userId);

    /**
     * 查询用户拥有的项目
     */
    @Query("SELECT p FROM Project p WHERE p.ownerId = :ownerId AND p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    List<Project> findOwnerProjects(@Param("ownerId") Long ownerId);

    /**
     * 检查项目是否存在且属于指定所有者
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * 查询项目 ID 列表（根据所有者 ID）
     */
    @Query("SELECT p.id FROM Project p WHERE p.ownerId = :ownerId AND p.deletedAt IS NULL")
    List<Long> findIdsByOwnerId(@Param("ownerId") Long ownerId);
}
