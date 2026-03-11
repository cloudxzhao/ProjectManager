package com.projecthub.module.task.repository;

import com.projecthub.module.task.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 评论 Repository 接口 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  /** 查询任务下的评论列表（按创建时间升序） */
  @Query(
      "SELECT c FROM Comment c WHERE c.taskId = :taskId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
  List<Comment> findByTaskIdOrderByCreatedAtAsc(@Param("taskId") Long taskId);

  /** 查询评论的回复列表 */
  @Query(
      "SELECT c FROM Comment c WHERE c.parentId = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
  List<Comment> findByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId);

  /** 统计任务的评论数量 */
  @Query("SELECT COUNT(c) FROM Comment c WHERE c.taskId = :taskId AND c.deletedAt IS NULL")
  Long countByTaskId(@Param("taskId") Long taskId);
}
