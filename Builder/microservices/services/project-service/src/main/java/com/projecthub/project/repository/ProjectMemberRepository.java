package com.projecthub.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.project.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 项目成员 Repository
 */
@Mapper
public interface ProjectMemberRepository extends BaseMapper<ProjectMember> {

    /**
     * 根据项目ID查询成员
     */
    @Select("SELECT * FROM project_members WHERE project_id = #{projectId}")
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目和用户查询成员关系
     */
    @Select("SELECT * FROM project_members WHERE project_id = #{projectId} AND user_id = #{userId}")
    Optional<ProjectMember> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否是项目成员
     */
    @Select("SELECT COUNT(*) > 0 FROM project_members WHERE project_id = #{projectId} AND user_id = #{userId}")
    boolean isMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 统计项目成员数量
     */
    @Select("SELECT COUNT(*) FROM project_members WHERE project_id = #{projectId}")
    int countByProjectId(@Param("projectId") Long projectId);

}