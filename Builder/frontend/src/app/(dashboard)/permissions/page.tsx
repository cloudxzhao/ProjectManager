'use client';

import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Tag, Space, Modal, Form, Input, Select, message, Typography, Empty, Spin } from 'antd';
import { PlusOutlined, EyeOutlined, CheckOutlined, CloseOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getAvailablePermissions,
  getMyPermissionRequests,
  createPermissionRequest,
  getPermissionRequestDetail,
} from '@/lib/api/permission';
import type { AvailablePermission, PermissionRequest, PermissionRequestStatus } from '@/types/permission';

const { Title } = Typography;
const { TextArea } = Input;

// 状态映射
const statusMap: Record<PermissionRequestStatus, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待审批' },
  APPROVED: { color: 'green', text: '已通过' },
  REJECTED: { color: 'red', text: '已拒绝' },
};

/** 权限申请页面 */
const PermissionsPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [availablePermissions, setAvailablePermissions] = useState<AvailablePermission[]>([]);
  const [myRequests, setMyRequests] = useState<PermissionRequest[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [applyModalOpen, setApplyModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState<PermissionRequest | null>(null);
  const [form] = Form.useForm();

  // 加载可申请的权限列表
  const loadAvailablePermissions = async () => {
    try {
      setLoading(true);
      const data = await getAvailablePermissions();
      setAvailablePermissions(data);
    } catch (error) {
      console.error('加载可申请职位失败:', error);
      message.error('加载可申请职位失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载我的申请记录
  const loadMyRequests = async () => {
    try {
      setLoading(true);
      const result = await getMyPermissionRequests(page, pageSize, statusFilter);
      setMyRequests(result.list);
      setTotal(result.total);
    } catch (error) {
      console.error('加载申请记录失败:', error);
      message.error('加载申请记录失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAvailablePermissions();
    loadMyRequests();
  }, []);

  useEffect(() => {
    loadMyRequests();
  }, [page, pageSize, statusFilter]);

  // 提交申请
  const handleApply = async () => {
    try {
      await form.validateFields();
      const values = form.getFieldsValue();
      setSubmitting(true);

      await createPermissionRequest({
        permissionId: values.permissionId,
        reason: values.reason,
      });

      message.success('申请提交成功');
      setApplyModalOpen(false);
      form.resetFields();
      loadMyRequests();
      loadAvailablePermissions();
    } catch (error: any) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      } else if (error.message) {
        // 表单验证错误
      } else {
        message.error('提交申请失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // 查看详情
  const handleViewDetail = async (id: number) => {
    try {
      setLoading(true);
      const detail = await getPermissionRequestDetail(id);
      setSelectedRequest(detail);
      setDetailModalOpen(true);
    } catch (error) {
      message.error('加载申请详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 申请记录表格列
  const columns: ColumnsType<PermissionRequest> = [
    {
      title: '权限名称',
      dataIndex: 'permissionName',
      key: 'permissionName',
      width: 200,
    },
    {
      title: '权限编码',
      dataIndex: 'permissionCode',
      key: 'permissionCode',
      width: 180,
    },
    {
      title: '申请理由',
      dataIndex: 'reason',
      key: 'reason',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: PermissionRequestStatus) => (
        <Tag color={statusMap[status].color}>{statusMap[status].text}</Tag>
      ),
    },
    {
      title: '申请时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => new Date(createdAt).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record.id)}
        >
          详情
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 页面头部 */}
      <div className="flex items-center justify-between">
        <div>
          <Title level={2} className="!mb-2 !text-white">权限申请</Title>
          <p className="text-gray-400">申请系统权限，查看申请记录和审批状态</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setApplyModalOpen(true)}
          size="large"
        >
          申请权限
        </Button>
      </div>

      {/* 可申请权限概览 */}
      <Card className="glass-card">
        <div className="flex items-center justify-between mb-4">
          <Title level={5} className="!mb-0 !text-white">可申请的权限</Title>
          <Button type="text" icon={<ReloadOutlined />} onClick={loadAvailablePermissions} />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {loading ? (
            <div className="col-span-full flex justify-center py-8">
              <Spin size="large" />
            </div>
          ) : availablePermissions.length === 0 ? (
            <div className="col-span-full">
              <Empty description="暂无可申请的权限" />
            </div>
          ) : (
            availablePermissions.map((permission) => (
              <Card
                key={permission.id}
                size="small"
                className={`transition-all cursor-pointer ${
                  permission.hasPermission
                    ? 'bg-green-500/10 border-green-500/30'
                    : 'bg-white/5 border-white/10 hover:border-orange-500/50'
                }`}
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-white">{permission.name}</span>
                    {permission.hasPermission ? (
                      <Tag color="green">已拥有</Tag>
                    ) : (
                      <Tag color="blue">可申请</Tag>
                    )}
                  </div>
                  <p className="text-xs text-gray-400 font-mono">{permission.code}</p>
                  <p className="text-sm text-gray-500">{permission.description || '暂无描述'}</p>
                </div>
              </Card>
            ))
          )}
        </div>
      </Card>

      {/* 我的申请记录 */}
      <Card className="glass-card">
        <div className="flex items-center justify-between mb-4">
          <Title level={5} className="!mb-0 !text-white">我的申请记录</Title>
          <Select
            placeholder="状态筛选"
            allowClear
            className="w-40"
            options={[
              { label: '待审批', value: 'PENDING' },
              { label: '已通过', value: 'APPROVED' },
              { label: '已拒绝', value: 'REJECTED' },
            ]}
            onChange={(value) => setStatusFilter(value)}
            value={statusFilter}
          />
        </div>
        <Table
          columns={columns}
          dataSource={myRequests}
          rowKey="id"
          loading={loading}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setPage(page);
              setPageSize(pageSize);
            },
          }}
          locale={{ emptyText: <Empty description="暂无申请记录" /> }}
        />
      </Card>

      {/* 申请权限弹窗 */}
      <Modal
        title="申请权限"
        open={applyModalOpen}
        onOk={handleApply}
        onCancel={() => {
          setApplyModalOpen(false);
          form.resetFields();
        }}
        confirmLoading={submitting}
        width={520}
      >
        <Form form={form} layout="vertical" className="mt-4">
          <Form.Item
            name="permissionId"
            label="选择权限"
            rules={[{ required: true, message: '请选择要申请的权限' }]}
          >
            <Select
              placeholder="请选择权限"
              options={availablePermissions
                .filter((p) => !p.hasPermission)
                .map((p) => ({
                  label: `${p.name} (${p.code})`,
                  value: p.id,
                }))}
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          <Form.Item
            name="reason"
            label="申请理由"
            rules={[
              { required: true, message: '请填写申请理由' },
              { max: 500, message: '申请理由最多 500 字' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请详细说明申请该权限的原因和使用场景..."
              showCount
              maxLength={500}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 申请详情弹窗 */}
      <Modal
        title="申请详情"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={600}
      >
        {selectedRequest && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-400">权限名称</p>
                <p className="text-white font-medium">{selectedRequest.permissionName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-400">权限编码</p>
                <p className="text-white font-mono">{selectedRequest.permissionCode}</p>
              </div>
              <div>
                <p className="text-sm text-gray-400">申请人</p>
                <p className="text-white">{selectedRequest.nickname || selectedRequest.username}</p>
              </div>
              <div>
                <p className="text-sm text-gray-400">状态</p>
                <Tag color={statusMap[selectedRequest.status].color}>
                  {statusMap[selectedRequest.status].text}
                </Tag>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-gray-400">申请理由</p>
                <p className="text-white">{selectedRequest.reason}</p>
              </div>
              <div>
                <p className="text-sm text-gray-400">申请时间</p>
                <p className="text-white">{new Date(selectedRequest.createdAt).toLocaleString('zh-CN')}</p>
              </div>
              {selectedRequest.updatedAt && (
                <div>
                  <p className="text-sm text-gray-400">更新时间</p>
                  <p className="text-white">{new Date(selectedRequest.updatedAt).toLocaleString('zh-CN')}</p>
                </div>
              )}
            </div>

            {/* 审批记录 */}
            {selectedRequest.approvalRecords && selectedRequest.approvalRecords.length > 0 && (
              <div className="mt-6">
                <p className="text-sm font-medium text-gray-400 mb-3">审批记录</p>
                <div className="space-y-3">
                  {selectedRequest.approvalRecords.map((record) => (
                    <Card
                      key={record.id}
                      size="small"
                      className={`border-l-4 ${
                        record.action === 'APPROVE'
                          ? 'border-l-green-500 bg-green-500/5'
                          : 'border-l-red-500 bg-red-500/5'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          {record.action === 'APPROVE' ? (
                            <CheckOutlined className="text-green-500" />
                          ) : (
                            <CloseOutlined className="text-red-500" />
                          )}
                          <span className="font-medium text-white">
                            {record.approverNickname || record.approverName}
                          </span>
                          <Tag color={record.action === 'APPROVE' ? 'green' : 'red'}>
                            {record.action === 'APPROVE' ? '同意' : '拒绝'}
                          </Tag>
                        </div>
                        <span className="text-xs text-gray-400">
                          {new Date(record.createdAt).toLocaleString('zh-CN')}
                        </span>
                      </div>
                      {record.comment && (
                        <p className="text-sm text-gray-300">{record.comment}</p>
                      )}
                    </Card>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PermissionsPage;
