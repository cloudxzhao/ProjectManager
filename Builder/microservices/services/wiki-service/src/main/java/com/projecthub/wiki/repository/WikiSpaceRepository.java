package com.projecthub.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.wiki.entity.WikiSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WikiSpaceRepository extends BaseMapper<WikiSpace> {
    @Select("SELECT * FROM wiki_spaces WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<WikiSpace> findByProjectId(@Param("projectId") Long projectId);
}