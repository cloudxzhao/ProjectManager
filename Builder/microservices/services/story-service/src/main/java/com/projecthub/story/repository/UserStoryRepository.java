package com.projecthub.story.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.story.entity.UserStory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserStoryRepository extends BaseMapper<UserStory> {
    @Select("SELECT * FROM user_stories WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<UserStory> findByProjectId(@Param("projectId") Long projectId);
}