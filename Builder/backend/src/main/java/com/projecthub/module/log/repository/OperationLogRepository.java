package com.projecthub.module.log.repository;

import com.projecthub.module.log.entity.OperationLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 操作日志 Repository 接口 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

  /** 查询用户的操作日志 */
  @Query("SELECT l FROM OperationLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
  Page<OperationLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

  /** 查询模块的操作日志 */
  @Query("SELECT l FROM OperationLog l WHERE l.module = :module ORDER BY l.createdAt DESC")
  Page<OperationLog> findByModule(@Param("module") String module, Pageable pageable);

  /** 查询最近的操作日志 */
  @Query("SELECT l FROM OperationLog l ORDER BY l.createdAt DESC")
  List<OperationLog> findRecentLogs(Pageable pageable);
}