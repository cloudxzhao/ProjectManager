package com.projecthub.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.wiki.entity.WikiPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WikiPageRepository extends BaseMapper<WikiPage> {
    @Select("SELECT * FROM wiki_pages WHERE space_id = #{spaceId} AND deleted = 0 ORDER BY order_num ASC, created_at DESC")
    List<WikiPage> findBySpaceId(@Param("spaceId") Long spaceId);

    @Select("SELECT * FROM wiki_pages WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY order_num ASC")
    List<WikiPage> findByParentId(@Param("parentId") Long parentId);
}