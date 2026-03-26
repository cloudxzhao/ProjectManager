package com.projecthub.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projecthub.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务 Repository
 */
@Mapper
public interface TaskRepository extends BaseMapper<Task> {

    /**
     * 根据项目ID查询任务
     */
    @Select("SELECT * FROM tasks WHERE project_id = #{projectId} AND deleted = 0 ORDER BY sort_order, created_at DESC")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目和状态查询任务
     */
    @Select("SELECT * FROM tasks WHERE project_id = #{projectId} AND status = #{status} AND deleted = 0 ORDER BY sort_order")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    /**
     * 根据负责人查询任务
     */
    @Select("SELECT * FROM tasks WHERE assignee_id = #{assigneeId} AND deleted = 0 ORDER BY created_at DESC")
    List<Task> findByAssigneeId(@Param("assigneeId") Long assigneeId);

    /**
     * 更新任务状态
     */
    @Update("UPDATE tasks SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新任务排序
     */
    @Update("UPDATE tasks SET status = #{status}, sort_order = #{sortOrder}, column_id = #{columnId}, updated_at = NOW() WHERE id = #{id}")
    int updatePosition(@Param("id") Long id, @Param("status") String status, @Param("sortOrder") Integer sortOrder, @Param("columnId") Long columnId);

    /**
     * 获取项目下一个任务编号
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(task_key FROM 6) AS INTEGER)), 0) + 1 FROM tasks WHERE project_id = #{projectId}")
    int getNextTaskNumber(@Param("projectId") Long projectId);

}