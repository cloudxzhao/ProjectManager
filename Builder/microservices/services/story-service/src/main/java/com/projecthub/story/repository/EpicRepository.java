package com.projecthub.story.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.story.entity.Epic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface EpicRepository extends BaseMapper<Epic> {
    @Select("SELECT * FROM epics WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<Epic> findByProjectId(@Param("projectId") Long projectId);
}