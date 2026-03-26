package com.projecthub.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目 Repository
 */
@Mapper
public interface ProjectRepository extends BaseMapper<Project> {

    /**
     * 根据状态查询项目
     */
    @Select("SELECT * FROM projects WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Project> findByStatus(@Param("status") String status);

    /**
     * 根据负责人查询项目
     */
    @Select("SELECT * FROM projects WHERE owner_id = #{ownerId} AND deleted = 0 ORDER BY created_at DESC")
    List<Project> findByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 根据成员ID查询项目（通过项目成员表关联）
     */
    @Select("SELECT p.* FROM projects p " +
            "JOIN project_members pm ON p.id = pm.project_id " +
            "WHERE pm.user_id = #{userId} AND p.deleted = 0 " +
            "ORDER BY p.created_at DESC")
    List<Project> findByMemberId(@Param("userId") Long userId);

}