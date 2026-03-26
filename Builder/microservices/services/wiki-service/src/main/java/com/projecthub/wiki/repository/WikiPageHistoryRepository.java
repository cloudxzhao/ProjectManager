package com.projecthub.wiki.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.wiki.entity.WikiPageHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Wiki 页面历史 Repository
 */
@Mapper
public interface WikiPageHistoryRepository extends BaseMapper<WikiPageHistory> {

    /**
     * 查询页面的历史版本列表
     */
    default List<WikiPageHistory> findByPageId(Long pageId) {
        LambdaQueryWrapper<WikiPageHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WikiPageHistory::getPageId, pageId)
                .orderByDesc(WikiPageHistory::getVersion);
        return selectList(wrapper);
    }

    /**
     * 查询页面的指定版本
     */
    default WikiPageHistory findByPageIdAndVersion(Long pageId, Integer version) {
        LambdaQueryWrapper<WikiPageHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WikiPageHistory::getPageId, pageId)
                .eq(WikiPageHistory::getVersion, version);
        return selectOne(wrapper);
    }

    /**
     * 查询当前版本
     */
    default WikiPageHistory findCurrentByPageId(Long pageId) {
        LambdaQueryWrapper<WikiPageHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WikiPageHistory::getPageId, pageId)
                .eq(WikiPageHistory::getIsCurrent, true);
        return selectOne(wrapper);
    }

    /**
     * 获取最大版本号
     */
    @Select("SELECT COALESCE(MAX(version), 0) FROM wiki_page_history WHERE page_id = #{pageId}")
    Integer getMaxVersion(@Param("pageId") Long pageId);
}
