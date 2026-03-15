'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Card, Avatar, Tag, Button, Table, Modal, Form, Select, Input, message, Popconfirm, Space, Spin } from 'antd';
import { PlusOutlined, UserAddOutlined } from '@ant-design/icons';
import { getProjectMembers, addProjectMember, removeProjectMember, updateProjectMemberRole } from '@/lib/api/project';
import { searchUsers } from '@/lib/api/user';
import type { MemberRole } from '@/types/project';

const { Option } = Select;

// 前端成员数据结构
interface Member {
  id: string;
  userId: string;
  name: string;
  email: string;
  avatar: string | null;
  role: MemberRole;
  joinedAt: string;
}

const roleColorMap: Record<string, string> = {
  OWNER: 'purple',
  ADMIN: 'red',
  MANAGER: 'blue',
  MEMBER: 'gray',
};

const roleTextMap: Record<string, string> = {
  OWNER: '项目所有者',
  ADMIN: '管理员',
  MANAGER: '项目经理',
  MEMBER: '普通成员',
};

interface AddMemberFormValues {
  userId: number;
  role: string;
}

export default function ProjectMembersPage() {
  const params = useParams();
  const projectIdNum = Number(params.id);

  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingMember, setEditingMember] = useState<Member | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);
  const [userOptions, setUserOptions] = useState<{ value: number; label: string; email: string }[]>([]);
  const [form] = Form.useForm();

  // 加载成员列表
  useEffect(() => {
    const loadMembers = async () => {
      if (!projectIdNum || isNaN(projectIdNum)) return;
      setLoading(true);
      try {
        const data = await getProjectMembers(projectIdNum);
        // 将后端数据转换为前端格式
        const memberList: Member[] = (data || []).map((item, index) => ({
          id: String(item.id || index + 1),
          userId: String(item.userId),
          name: item.nickname || item.username || `用户 ${item.userId}`,
          email: item.email || '',
          avatar: item.avatar || null,
          role: item.role as MemberRole,
          joinedAt: item.joinedAt ? new Date(item.joinedAt).toISOString().split('T')[0] : '',
        }));
        setMembers(memberList);
      } catch (error) {
        console.error('加载成员列表失败:', error);
        message.error('加载成员列表失败');
      } finally {
        setLoading(false);
      }
    };
    loadMembers();
  }, [projectIdNum]);

  // 搜索用户（用于添加成员）
  useEffect(() => {
    const timer = setTimeout(async () => {
      if (searchKeyword.trim()) {
        setSearchLoading(true);
        try {
          const users = await searchUsers(searchKeyword);
          // 过滤掉已经是成员的用户
          const memberUserIds = new Set(members.map(m => m.userId));
          const filteredUsers = users
            .filter(u => !memberUserIds.has(String(u.id)))
            .map(u => ({
              value: u.id,
              label: u.nickname || u.username || `用户 ${u.id}`,
              email: u.email || '',
            }));
          setUserOptions(filteredUsers);
        } catch (error) {
          console.error('搜索用户失败:', error);
        } finally {
          setSearchLoading(false);
        }
      } else {
        setUserOptions([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchKeyword, members]);

  // 添加成员
  const handleAddMember = async (values: AddMemberFormValues) => {
    setLoading(true);
    try {
      await addProjectMember(projectIdNum, {
        userId: values.userId,
        role: values.role as MemberRole,
      });

      // 重新加载成员列表
      const data = await getProjectMembers(projectIdNum);
      const memberList: Member[] = (data || []).map((item, index) => ({
        id: String(item.id || index + 1),
        userId: String(item.userId),
        name: item.nickname || item.username || `用户 ${item.userId}`,
        email: item.email || '',
        avatar: item.avatar || null,
        role: item.role as MemberRole,
        joinedAt: item.joinedAt ? new Date(item.joinedAt).toISOString().split('T')[0] : '',
      }));
      setMembers(memberList);

      message.success('成员添加成功');
      setAddModalOpen(false);
      setSearchKeyword('');
      setUserOptions([]);
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
      await updateProjectMemberRole(projectIdNum, Number(editingMember.userId), values.role as MemberRole);

      setMembers(members.map((m) =>
        m.userId === editingMember.userId ? { ...m, role: values.role as MemberRole } : m
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
  const handleRemoveMember = async (memberUserId: string, memberName: string, memberRole: string) => {
    setLoading(true);
    try {
      if (memberRole === 'OWNER') {
        message.error('不能移除项目所有者');
        return;
      }

      await removeProjectMember(projectIdNum, Number(memberUserId));

      setMembers(members.filter((m) => m.userId !== memberUserId));
      message.success('成员已移除');
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '移除失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 打开编辑对话框
  const openEditModal = (member: Member) => {
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
      render: (_: unknown, record: Member) => (
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
      render: (_: unknown, record: Member) => (
        <Space size="middle">
          {record.role !== 'OWNER' && (
            <>
              <button
                onClick={() => openEditModal(record)}
                className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
                style={{ color: '#636e72' }}
                title="编辑"
              >
                ✏️
              </button>
              <Popconfirm
                title="确认移除"
                description={`确定要移除成员 ${record.name} 吗？`}
                onConfirm={() => handleRemoveMember(record.userId, record.name, record.role)}
                okText="确认"
                cancelText="取消"
              >
                <button
                  className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
                  style={{ color: '#636e72' }}
                  title="移除"
                >
                  🗑️
                </button>
              </Popconfirm>
            </>
          )}
          {record.role === 'OWNER' && (
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
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-white">{members.length}</div>
            <div className="text-gray-400 text-sm mt-1">总成员数</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-red-400">
              {members.filter((m) => m.role === 'OWNER').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">项目所有者</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-purple-400">
              {members.filter((m) => m.role === 'ADMIN').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">管理员</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-blue-400">
              {members.filter((m) => m.role === 'MANAGER').length}
            </div>
            <div className="text-gray-400 text-sm mt-1">项目经理</div>
          </div>
        </Card>
        <Card className="bg-gray-800/50 border-gray-700">
          <div className="text-center">
            <div className="text-3xl font-bold text-gray-400">
              {members.filter((m) => m.role === 'MEMBER').length}
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
          loading={loading}
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
          setSearchKeyword('');
          setUserOptions([]);
          form.resetFields();
        }}
        okButtonProps={{ loading }}
        okText="确认添加"
        cancelText="取消"
        className="dark-modal"
        width={500}
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          onFinish={handleAddMember}
          size="large"
          initialValues={{ role: 'MEMBER' }}
        >
          <Form.Item
            name="userId"
            label="搜索用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select
              placeholder="输入用户名、昵称或邮箱搜索"
              className="bg-gray-700 border-gray-600"
              showSearch
              filterOption={false}
              onSearch={setSearchKeyword}
              notFoundContent={searchLoading ? <Spin size="small" /> : null}
              options={userOptions.map((user) => ({
                value: user.value,
                label: (
                  <div className="flex items-center justify-between">
                    <span>{user.label}</span>
                    <span className="text-gray-400 text-sm">{user.email}</span>
                  </div>
                ),
              }))}
              dropdownRender={(menu) => (
                <div>
                  {menu}
                  {searchKeyword && (
                    <div className="px-4 py-2 text-xs text-gray-500 border-t border-gray-700">
                      搜索关键词：{searchKeyword}
                    </div>
                  )}
                </div>
              )}
            />
          </Form.Item>

          <Form.Item
            name="role"
            label="成员角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select className="bg-gray-700 border-gray-600">
              <Option value="ADMIN">管理员</Option>
              <Option value="MANAGER">项目经理</Option>
              <Option value="MEMBER">普通成员</Option>
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
                <div className="text-gray-500 text-xs mt-1">当前角色：{roleTextMap[editingMember.role]}</div>
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
                  <Option value="ADMIN">管理员</Option>
                  <Option value="MANAGER">项目经理</Option>
                  <Option value="MEMBER">普通成员</Option>
                </Select>
              </Form.Item>
            </Form>
          </div>
        )}
      </Modal>
    </div>
  );
}
