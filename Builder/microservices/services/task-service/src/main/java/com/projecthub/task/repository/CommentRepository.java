package com.projecthub.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.task.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论 Repository
 */
@Mapper
public interface CommentRepository extends BaseMapper<Comment> {

    /**
     * 根据任务ID查询评论
     */
    @Select("SELECT * FROM comments WHERE task_id = #{taskId} AND deleted = 0 ORDER BY created_at ASC")
    List<Comment> findByTaskId(@Param("taskId") Long taskId);

}