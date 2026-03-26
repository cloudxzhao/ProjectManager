package com.projecthub.issue.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.issue.entity.Issue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface IssueRepository extends BaseMapper<Issue> {
    @Select("SELECT * FROM issues WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<Issue> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM issues WHERE task_id = #{taskId} AND deleted = 0 ORDER BY created_at DESC")
    List<Issue> findByTaskId(@Param("taskId") Long taskId);
}