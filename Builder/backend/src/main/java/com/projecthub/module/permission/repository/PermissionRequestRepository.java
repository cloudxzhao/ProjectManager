package com.projecthub.module.permission.repository;

import com.projecthub.module.permission.entity.PermissionRequest;
import com.projecthub.module.permission.entity.PermissionRequest.RequestStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 权限申请 Repository 接口 */
@Repository
public interface PermissionRequestRepository
    extends JpaRepository<PermissionRequest, Long>, JpaSpecificationExecutor<PermissionRequest> {

  /** 查询用户的申请记录 */
  Page<PermissionRequest> findByUserId(Long userId, Pageable pageable);

  /** 查询用户的申请记录（按状态筛选） */
  Page<PermissionRequest> findByUserIdAndStatus(
      Long userId, RequestStatus status, Pageable pageable);

  /** 查询待审批的申请列表 */
  @Query(
      "SELECT pr FROM PermissionRequest pr "
          + "WHERE pr.status = 'PENDING' "
          + "ORDER BY pr.createdAt DESC")
  Page<PermissionRequest> findPendingRequests(Pageable pageable);

  /** 查询所有申请记录（支持状态筛选） */
  Page<PermissionRequest> findByStatus(RequestStatus status, Pageable pageable);

  /** 查询用户指定权限的申请记录 */
  List<PermissionRequest> findByUserIdAndPermissionId(Long userId, Long permissionId);

  /** 查询用户指定权限的待审批申请 */
  @Query(
      "SELECT pr FROM PermissionRequest pr "
          + "WHERE pr.userId = :userId "
          + "AND pr.permissionId = :permissionId "
          + "AND pr.status = 'PENDING'")
  List<PermissionRequest> findPendingByUserIdAndPermissionId(
      @Param("userId") Long userId, @Param("permissionId") Long permissionId);
}
