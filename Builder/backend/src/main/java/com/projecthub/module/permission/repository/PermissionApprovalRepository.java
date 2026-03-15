package com.projecthub.module.permission.repository;

import com.projecthub.module.permission.entity.PermissionApproval;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 权限审批记录 Repository 接口 */
@Repository
public interface PermissionApprovalRepository extends JpaRepository<PermissionApproval, Long> {

  /** 查询申请的审批记录 */
  List<PermissionApproval> findByRequestIdOrderByCreatedAtAsc(Long requestId);

  /** 查询审批人参与的所有审批记录 */
  @Query(
      "SELECT pa FROM PermissionApproval pa "
          + "WHERE pa.approverId = :approverId "
          + "ORDER BY pa.createdAt DESC")
  List<PermissionApproval> findByApproverId(@Param("approverId") Long approverId);

  /** 查询申请的最新审批记录 */
  @Query(
      "SELECT pa FROM PermissionApproval pa "
          + "WHERE pa.requestId = :requestId "
          + "ORDER BY pa.createdAt DESC")
  List<PermissionApproval> findLatestByRequestId(@Param("requestId") Long requestId);
}
