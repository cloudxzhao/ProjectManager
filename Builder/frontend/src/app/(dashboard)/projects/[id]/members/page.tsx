'use client';

import { useState } from 'react';
import { useParams } from 'next/navigation';
import { Card, Avatar, Tag, Button, Table, Modal, Form, Select, Input, message, Popconfirm, Space } from 'antd';
import { PlusOutlined, DeleteOutlined, EditOutlined, UserAddOutlined } from '@ant-design/icons';
import { api } from '@/lib/api/axios';
import { endpoints } from '@/lib/api/endpoints';

const { Option } = Select;

// Mock 数据
const initialMembers = [
  { id: '1', userId: 'u1', name: '张三', email: 'zhangsan@example.com', avatar: null, role: 'admin', joinedAt: '2024-01-01' },
  { id: '2', userId: 'u2', name: '李四', email: 'lisi@example.com', avatar: null, role: 'manager', joinedAt: '2024-01-05' },
  { id: '3', userId: 'u3', name: '王五', email: 'wangwu@example.com', avatar: null, role: 'member', joinedAt: '2024-01-10' },
  { id: '4', userId: 'u4', name: '赵六', email: 'zhaoliu@example.com', avatar: null, role: 'member', joinedAt: '2024-01-15' },
];

const roleColorMap: Record<string, string> = {
  admin: 'purple',
  manager: 'blue',
  member: 'gray',
};

const roleTextMap: Record<string, string> = {
  admin: '管理员',
  manager: '项目经理',
  member: '成员',
};

interface AddMemberFormValues {
  userId: string;
  role: string;
}

export default function ProjectMembersPage() {
  const params = useParams();
  const projectId = params.id as string;

  const [members, setMembers] = useState(initialMembers);
  const [loading, setLoading] = useState(false);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingMember, setEditingMember] = useState<typeof initialMembers[0] | null>(null);
  const [form] = Form.useForm();

  // 添加成员
  const handleAddMember = async (values: AddMemberFormValues) => {
    setLoading(true);
    try {
      // 模拟 API 调用
      await new Promise((resolve) => setTimeout(resolve, 500));

      const newMember = {
        id: String(members.length + 1),
        userId: values.userId,
        name: `用户${values.userId}`,
        email: `user${values.userId}@example.com`,
        avatar: null,
        role: values.role,
        joinedAt: new Date().toISOString().split('T')[0],
      };

      setMembers([...members, newMember]);
      message.success('成员添加成功');
      setAddModalOpen(false);
      form.resetFields();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '添加失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 修改成员角色
  const handleEditRole = async (values: AddMemberFormValues) => {
    if (!editingMember) return;

    setLoading(true);
    try {
      await api.put(endpoints.project.addMember(projectId), {
        userId: editingMember.userId,
        role: values.role,
      });

      setMembers(members.map((m) =>
        m.id === editingMember.id ? { ...m, role: values.role } : m
      ));

      message.success('角色修改成功');
      setEditModalOpen(false);
      setEditingMember(null);
      form.resetFields();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '修改失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 移除成员
  const handleRemoveMember = async (memberId: string, memberIdName: string) => {
    setLoading(true);
    try {
      const member = members.find((m) => m.id === memberId);
      if (member?.role === 'admin') {
        message.error('不能移除项目管理员');
        return;
      }

      await api.delete(endpoints.project.removeMember(projectId, memberId));
      setMembers(members.filter((m) => m.id !== memberId));
      message.success('成员已移除');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '移除失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 打开编辑对话框
  const openEditModal = (member: typeof initialMembers[0]) => {
    setEditingMember(member);
    form.setFieldsValue({ role: member.role });
    setEditModalOpen(true);
  };

  // 表格列定义
  const columns = [
    {
      title: '成员',
      dataIndex: 'name',
      key: 'name',
      render: (_: unknown, record: typeof initialMembers[0]) => (
        <div className="flex items-center gap-3">
          <Avatar
            size={40}
            className="bg-gradient-to-br from-orange-400 to-amber-500"
          >
            {record.name[0]}
          </Avatar>
          <div>
            <div className="font-medium text-white">{record.name}</div>
            <div className="text-sm text-gray-400">{record.email}</div>
          </div>
        </div>
      ),
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => (
        <Tag color={roleColorMap[role]}>{roleTextMap[role]}</Tag>
      ),
    },
    {
      title: '加入时间',
      dataIndex: 'joinedAt',
      key: 'joinedAt',
      render: (date: string) => (
        <span className="text-gray-400">{date}</span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: typeof initialMembers[0]) => (
        <Space size="middle">
          {record.role !== 'admin' && (
            <>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => openEditModal(record)}
                className="text-gray-400 hover:text-white"
              >
                编辑
              </Button>
              <Popconfirm
                title="确认移除"
                description={`确定要移除成员 ${record.name} 吗？`}
                onConfirm={() => handleRemoveMember(record.id, record.name)}
                okText="确认"
                cancelText="取消"
              >
                <Button
                  type="link"
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                  className="text-gray-400"
                >
                  移除
                </Button>
              </Popconfirm>
            </>
          )}
          {record.role === 'admin' && (
            <span className="text-gray-500 text-sm">不可操作</span>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">成员管理</h1>
          <p className="text-gray-400 mt-1">管理项目成员和角色权限</p>
        </div>
        <Button
          type="primary"
          icon={<UserAddOutlined />}
          onClick={() => setAddModalOpen(true)}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
        >
          添加成员
        </Button>
      </div>

      {/* 成员统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-white">{members.length}</div>
            <div className="text-gray-400 text-sm mt-1">总成员数</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-purple-400">
              {members.filter((m) => m.role === 'admin').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">管理员</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-blue-400">
              {members.filter((m) => m.role === 'manager').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">项目经理</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-gray-400">
              {members.filter((m) => m.role === 'member').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">普通成员</div>
          </div>
        </Card>
      </div>

      {/* 成员列表表格 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <Table
          columns={columns}
          dataSource={members}
          rowKey="id"
          pagination={false}
          className="project-members-table"
          scroll={{ x: 600 }}
        />
      </Card>

      {/* 添加成员对话框 */}
      <Modal
        title="添加成员"
        open={addModalOpen}
        onOk={() => form.submit()}
        onCancel={() => {
          setAddModalOpen(false);
          form.resetFields();
        }}
        okButtonProps={{ loading }}
        okText="确认添加"
        cancelText="取消"
        className="dark-modal"
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          onFinish={handleAddMember}
          size="large"
          initialValues={{ role: 'member' }}
        >
          <Form.Item
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select
              placeholder="搜索并选择用户"
              className="bg-gray-700 border-gray-600"
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              options={[
                { value: '101', label: '用户 101 - user101@example.com' },
                { value: '102', label: '用户 102 - user102@example.com' },
                { value: '103', label: '用户 103 - user103@example.com' },
                { value: '104', label: '用户 104 - user104@example.com' },
                { value: '105', label: '用户 105 - user105@example.com' },
              ]}
            />
          </Form.Item>

          <Form.Item
            name="role"
            label="成员角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select className="bg-gray-700 border-gray-600">
              <Option value="admin">管理员</Option>
              <Option value="manager">项目经理</Option>
              <Option value="member">普通成员</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑角色对话框 */}
      <Modal
        title="修改角色"
        open={editModalOpen}
        onOk={() => form.submit()}
        onCancel={() => {
          setEditModalOpen(false);
          setEditingMember(null);
          form.resetFields();
        }}
        okButtonProps={{ loading }}
        okText="确认修改"
        cancelText="取消"
        className="dark-modal"
      >
        {editingMember && (
          <div>
            <div className="flex items-center gap-3 mb-6 p-4 bg-gray-700/30 rounded-lg">
              <Avatar
                size={48}
                className="bg-gradient-to-br from-orange-400 to-amber-500"
              >
                {editingMember.name[0]}
              </Avatar>
              <div>
                <div className="font-medium text-white text-lg">{editingMember.name}</div>
                <div className="text-gray-400 text-sm">{editingMember.email}</div>
              </div>
            </div>

            <Form
              form={form}
              layout="vertical"
              requiredMark={false}
              onFinish={handleEditRole}
              size="large"
            >
              <Form.Item
                name="role"
                label="成员角色"
                rules={[{ required: true, message: '请选择角色' }]}
              >
                <Select className="bg-gray-700 border-gray-600">
                  <Option value="admin">管理员</Option>
                  <Option value="manager">项目经理</Option>
                  <Option value="member">普通成员</Option>
                </Select>
              </Form.Item>
            </Form>
          </div>
        )}
      </Modal>
    </div>
  );
}
