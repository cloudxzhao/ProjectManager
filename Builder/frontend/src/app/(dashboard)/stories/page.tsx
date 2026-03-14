'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, Pagination, Modal, Form, Drawer, message, FormProps } from 'antd';
import { FileTextOutlined, PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { getStories, searchStories, deleteStory, createStory, updateStory, type UserStory, type StoryStatus, type Priority, type CreateUserStoryDto, type UpdateUserStoryDto, statusTextMap, priorityTextMap, statusMap, priorityMap } from '@/lib/api/story';
import { getProjects, getProjectMembers, type ProjectMemberResponse } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';
import { useAuth } from '@/lib/hooks/useAuth';

const { Option } = Select;
const { TextArea } = Input;

// 状态颜色映射（支持大写和小写）
const statusColorMap: Record<string, string> = {
  todo: 'default',
  in_progress: 'processing',
  testing: 'warning',
  done: 'success',
  TODO: 'default',
  IN_PROGRESS: 'processing',
  TESTING: 'warning',
  DONE: 'success',
};

// 状态文本映射（支持大写和小写）
const statusTextLabelMap: Record<string, string> = {
  todo: '待办',
  in_progress: '进行中',
  testing: '测试中',
  done: '已完成',
  TODO: '待办',
  IN_PROGRESS: '进行中',
  TESTING: '测试中',
  DONE: '已完成',
};

// 优先级颜色映射（支持大写和小写）
const priorityColorMap: Record<string, string> = {
  low: 'gray',
  medium: 'blue',
  high: 'orange',
  urgent: 'red',
  LOW: 'gray',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
};

// 优先级文本映射（支持大写和小写）
const priorityTextLabelMap: Record<string, string> = {
  low: '低',
  medium: '中',
  high: '高',
  urgent: '紧急',
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急',
};

// 项目图标
const projectIcons = ['🛒', '📱', '📊', '🤝', '🌐', '🔧', '💼', '🎯', '🚀', '💡'];

interface StoryCardProps {
  story: UserStory;
  project?: Project;
  onView: (story: UserStory) => void;
  onEdit: (story: UserStory) => void;
  onDelete: (story: UserStory) => void;
}

// 用户故事卡片组件
const StoryCard: React.FC<StoryCardProps> = ({ story, project, onView, onEdit, onDelete }) => {
  return (
    <Card
      hoverable
      className="h-full overflow-hidden transition-all duration-300 hover:-translate-y-1.5 group"
      style={{
        backgroundColor: 'rgba(255, 255, 255, 0.03)',
        borderColor: 'rgba(255, 255, 255, 0.08)',
      }}
      bodyStyle={{ padding: 0 }}
    >
      {/* 顶部渐变条 */}
      <div
        className="h-1 w-full"
        style={{
          background: `linear-gradient(90deg, ${project?.color || '#f97316'} 0%, ${project?.color || '#f97316'}cc 100%)`,
          opacity: 0.8,
        }}
      />

      <div className="p-5">
        {/* 项目和状态 */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2">
            {project && (
              <div
                className="w-8 h-8 rounded-lg flex items-center justify-center text-sm"
                style={{
                  background: `linear-gradient(135deg, ${project.color} 0%, ${project.color}cc 100%)`,
                  boxShadow: `0 4px 8px -2px ${project.color}40`,
                }}
              >
                {project.icon || '📁'}
              </div>
            )}
            <Tag color={statusColorMap[story.status]}>{statusTextLabelMap[story.status]}</Tag>
          </div>
          <Tag color={priorityColorMap[story.priority]}>{priorityTextLabelMap[story.priority]}</Tag>
        </div>

        {/* 标题 */}
        <h3 className="text-base font-semibold text-white mb-2 line-clamp-2">{story.title}</h3>

        {/* 描述 */}
        <p className="text-gray-400 text-sm mb-4 line-clamp-2">{story.description}</p>

        {/* 故事点 */}
        {story.storyPoints && (
          <div className="mb-4">
            <span className="text-xs text-gray-500 mr-2">故事点:</span>
            <span className="text-orange-400 font-medium">{story.storyPoints} pts</span>
          </div>
        )}

        {/* 底部：负责人和操作 */}
        <div className="flex items-center justify-between pt-4 border-t border-gray-700">
          <Avatar
            size={28}
            className="bg-gradient-to-br from-purple-400 to-pink-500"
            icon={!story.assigneeId && <span className="text-xs">?</span>}
          >
            {story.assigneeId ? (story.assigneeName?.charAt(0).toUpperCase() || `U${story.assigneeId}`) : ''}
          </Avatar>
          <div className="flex items-center gap-1">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => onView(story)}
              className="text-gray-400 hover:text-white"
            />
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => onEdit(story)}
              className="text-gray-400 hover:text-white"
            />
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={() => onDelete(story)}
              className="text-gray-400 hover:text-red-400"
            />
          </div>
        </div>
      </div>
    </Card>
  );
};

// 创建/编辑表单接口
interface StoryFormValues {
  projectId?: number;  // 仅用于选择项目成员，不传给 API
  title: string;
  description?: string;
  acceptanceCriteria?: string;
  status?: StoryStatus | string;
  priority?: Priority | string;
  assigneeId?: number;
  storyPoints?: number;
}

export default function StoriesPage() {
  const { user } = useAuth();
  const [stories, setStories] = useState<UserStory[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectMembers, setProjectMembers] = useState<ProjectMemberResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(10);
  const [selectedProject, setSelectedProject] = useState<number | undefined>(undefined);
  const [selectedStatus, setSelectedStatus] = useState<StoryStatus | undefined>(undefined);
  const [searchText, setSearchText] = useState('');

  // 模态框状态
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [detailDrawerOpen, setDetailDrawerOpen] = useState(false);

  // 当前操作的故事
  const [deletingStory, setDeletingStory] = useState<UserStory | null>(null);
  const [editingStory, setEditingStory] = useState<UserStory | null>(null);
  const [selectedStory, setSelectedStory] = useState<UserStory | null>(null);

  const [form] = Form.useForm();

  // 加载项目列表
  const fetchProjects = async () => {
    try {
      const result = await getProjects(1, 100);
      setProjects(result?.list || []);
    } catch (error) {
      console.error('加载项目列表失败:', error);
    }
  };

  // 加载项目成员列表
  const fetchProjectMembers = async (projectId: number) => {
    if (!projectId) return;
    try {
      const result = await getProjectMembers(projectId);
      setProjectMembers(result || []);

      // 如果是创建模式（没有 editingStory），自动设置当前用户为默认负责人
      if (!editingStory && user?.id) {
        // 检查当前用户是否是项目成员
        const isMember = result?.some((m) => m.user.id === user.id);
        if (isMember) {
          form.setFieldValue('assigneeId', user.id);
        }
      }
    } catch (error) {
      console.error('加载项目成员失败:', error);
    }
  };

  // 加载用户故事列表 - 使用全局搜索接口
  const fetchStories = async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = {
        page,
        size: pageSize,
      };
      if (selectedStatus) params.status = selectedStatus;
      if (searchText) params.keyword = searchText;
      // 如果选择了项目，通过 projectIds 参数筛选
      if (selectedProject) {
        params.projectIds = [selectedProject];
      }

      const result = await searchStories(params);
      setStories(result?.items || []);
      setTotal(result?.total || 0);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '加载用户故事失败';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    if (selectedProject) {
      fetchProjectMembers(selectedProject);
    } else {
      setProjectMembers([]);
    }
  }, [selectedProject]);

  useEffect(() => {
    fetchStories();
  }, [page, selectedProject, selectedStatus, searchText]);

  // 删除用户故事
  const handleDelete = async () => {
    if (!deletingStory) return;
    setLoading(true);
    try {
      await deleteStory(deletingStory.id);
      message.success('用户故事删除成功');
      setDeleteModalOpen(false);
      setDeletingStory(null);
      fetchStories();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '删除失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 打开创建表单
  const handleCreate = () => {
    setEditingStory(null);
    form.resetFields();
    // 默认选择当前筛选的项目
    if (selectedProject) {
      form.setFieldsValue({ projectId: selectedProject });
      // 如果已选择项目，自动设置当前用户为默认负责人
      if (user?.id && projectMembers.length > 0) {
        const isMember = projectMembers.some((m) => m.user.id === user.id);
        if (isMember) {
          form.setFieldValue('assigneeId', user.id);
        }
      }
    } else {
      // 如果没有选择项目，清空表单中的项目字段
      form.setFieldsValue({ projectId: undefined });
    }
    setFormModalOpen(true);
  };

  // 打开编辑表单
  const handleEdit = (story: UserStory) => {
    setEditingStory(story);
    // 编辑时加载对应项目的成员列表
    fetchProjectMembers(story.projectId);
    form.setFieldsValue({
      projectId: story.projectId,
      title: story.title,
      description: story.description,
      acceptanceCriteria: story.acceptanceCriteria,
      status: story.status?.toLowerCase(),  // 转小写用于表单
      priority: story.priority?.toLowerCase(),  // 转小写用于表单
      assigneeId: story.assigneeId,
      storyPoints: story.storyPoints,
    });
    setFormModalOpen(true);
  };

  // 查看详情
  const handleView = (story: UserStory) => {
    setSelectedStory(story);
    setDetailDrawerOpen(true);
  };

  // 表单提交
  const handleFormSubmit = async (values: StoryFormValues) => {
    setLoading(true);
    try {
      if (editingStory) {
        // 更新 - 将前端小写状态转换为后端大写
        const updateData: UpdateUserStoryDto = {
          title: values.title,
          description: values.description,
          acceptanceCriteria: values.acceptanceCriteria,
          status: values.status ? statusMap[String(values.status)] || String(values.status) as StoryStatus : undefined,
          priority: values.priority ? priorityMap[String(values.priority)] || String(values.priority) as Priority : undefined,
          assigneeId: values.assigneeId,
          storyPoints: values.storyPoints,
        };
        await updateStory(editingStory.id, updateData);
        message.success('用户故事更新成功');
      } else {
        // 创建 - status 字段由后端设置默认值 (TODO)
        const createData: CreateUserStoryDto = {
          title: values.title,
          description: values.description,
          acceptanceCriteria: values.acceptanceCriteria,
          priority: values.priority ? priorityMap[String(values.priority)] || 'MEDIUM' : 'MEDIUM',  // 默认为 MEDIUM
          assigneeId: values.assigneeId,
          storyPoints: values.storyPoints,
        };
        // 需要使用 selectedProject 或 values.projectId
        const targetProjectId = selectedProject || values.projectId;
        if (!targetProjectId) {
          message.error('请选择所属项目');
          setLoading(false);
          return;
        }
        await createStory(targetProjectId, createData);
        message.success('用户故事创建成功');
      }
      setFormModalOpen(false);
      fetchStories();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : (editingStory ? '更新失败' : '创建失败');
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 获取项目
  const getProject = (projectId: number) => {
    return projects.find((p) => p.id === projectId);
  };

  // 获取项目成员
  const getProjectMember = (userId?: number) => {
    if (!userId) return undefined;
    return projectMembers.find((m) => m.user.id === userId);
  };

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <FileTextOutlined className="text-orange-400" />
            用户故事
          </h1>
          <p className="text-gray-400 mt-1">管理和跟踪项目用户故事</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreate}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
        >
          新建故事
        </Button>
      </div>

      {/* 筛选区域 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <div className="flex flex-wrap gap-4">
          <div className="flex-1 min-w-[200px]">
            <Input
              placeholder="搜索故事标题或描述..."
              prefix={<SearchOutlined className="text-gray-400" />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={fetchStories}
              className="bg-gray-700/50 border-gray-600 text-white"
              allowClear
            />
          </div>
          <Select
            placeholder="选择项目"
            value={selectedProject}
            onChange={(value) => {
              setSelectedProject(value);
              setPage(1);
            }}
            className="w-[200px] bg-gray-700/50 border-gray-600"
            allowClear
          >
            {projects.map((project) => (
              <Option key={project.id} value={project.id}>
                {project.name}
              </Option>
            ))}
          </Select>
          <Select
            placeholder="选择状态"
            value={selectedStatus}
            onChange={(value) => {
              setSelectedStatus(value);
              setPage(1);
            }}
            className="w-[150px] bg-gray-700/50 border-gray-600"
            allowClear
          >
            <Option value="todo">待办</Option>
            <Option value="in_progress">进行中</Option>
            <Option value="testing">测试中</Option>
            <Option value="done">已完成</Option>
          </Select>
          <Button type="primary" onClick={fetchStories}>
            筛选
          </Button>
        </div>
      </Card>

      {/* 故事列表 - 卡片网格 */}
      {loading && stories.length === 0 ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" description="加载用户故事中..." />
        </div>
      ) : stories.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {stories.map((story) => (
              <StoryCard
                key={story.id}
                story={story}
                project={getProject(story.projectId)}
                onView={handleView}
                onEdit={handleEdit}
                onDelete={setDeletingStory}
              />
            ))}
          </div>
          {/* 分页组件 */}
          <div className="flex justify-end mt-4">
            <Pagination
              current={page}
              total={total}
              pageSize={pageSize}
              onChange={setPage}
              showSizeChanger={false}
              showTotal={(total) => `共 ${total} 条`}
              className="text-gray-400"
            />
          </div>
        </>
      ) : (
        <Card className="bg-gray-800/50 border-gray-700">
          <Empty description="暂无用户故事">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              创建第一个用户故事
            </Button>
          </Empty>
        </Card>
      )}

      {/* 删除确认对话框 */}
      <Modal
        title={
          <div className="flex items-center gap-3">
            <DeleteOutlined className="text-red-500 text-xl" />
            <span>确认删除</span>
          </div>
        }
        open={deleteModalOpen}
        onOk={handleDelete}
        onCancel={() => {
          setDeleteModalOpen(false);
          setDeletingStory(null);
        }}
        okButtonProps={{ danger: true, loading }}
        okText="确认删除"
        cancelText="取消"
        className="glass-dark"
      >
        <p className="text-gray-300">
          确定要删除用户故事 <span className="text-white font-semibold">{deletingStory?.title}</span> 吗？
        </p>
        <p className="text-red-400 mt-2">
          删除后不可恢复。
        </p>
      </Modal>

      {/* 创建/编辑表单对话框 */}
      <Modal
        title={editingStory ? '编辑用户故事' : '创建用户故事'}
        open={formModalOpen}
        onCancel={() => {
          setFormModalOpen(false);
          form.resetFields();
        }}
        footer={null}
        className="glass-dark"
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFormSubmit}
          size="large"
          initialValues={{
            priority: 'medium',
          }}
        >
          {/* 创建模式显示项目选择，编辑模式隐藏 */}
          {!editingStory && (
            <Form.Item
              name="projectId"
              label="所属项目"
              rules={[{ required: true, message: '请选择所属项目' }]}
            >
              <Select
                className="bg-gray-700/50 border-gray-600"
                placeholder="选择项目"
                onChange={(value) => {
                  // 项目改变时，重新加载项目成员
                  setSelectedProject(value);
                  fetchProjectMembers(value);
                }}
              >
                {projects.map((project) => (
                  <Option key={project.id} value={project.id}>
                    {project.icon || '📁'} {project.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          )}

          {editingStory && (
            <Form.Item label="所属项目">
              <div className="text-gray-400">{getProject(editingStory.projectId)?.icon} {getProject(editingStory.projectId)?.name}</div>
            </Form.Item>
          )}

          <Form.Item
            name="title"
            label="故事标题"
            rules={[{ required: true, message: '请输入故事标题' }]}
          >
            <Input
              placeholder="请输入故事标题"
              className="bg-gray-700/50 border-gray-600 text-white"
            />
          </Form.Item>

          <Form.Item
            name="description"
            label="故事描述"
          >
            <TextArea
              rows={4}
              placeholder="描述用户故事的内容..."
              className="bg-gray-700/50 border-gray-600 text-white"
              showCount
              maxLength={2000}
            />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            {editingStory && (
              <Form.Item
                name="status"
                label="状态"
              >
                <Select className="bg-gray-700/50 border-gray-600">
                  <Option value="todo">待办</Option>
                  <Option value="in_progress">进行中</Option>
                  <Option value="testing">测试中</Option>
                  <Option value="done">已完成</Option>
                </Select>
              </Form.Item>
            )}

            <Form.Item
              name="priority"
              label="优先级"
            >
              <Select className="bg-gray-700/50 border-gray-600">
                <Option value="low">低</Option>
                <Option value="medium">中</Option>
                <Option value="high">高</Option>
                <Option value="urgent">紧急</Option>
              </Select>
            </Form.Item>

            {editingStory && <div className="hidden" />}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="storyPoints"
              label="故事点"
            >
              <Select className="bg-gray-700/50 border-gray-600" allowClear>
                <Option value={1}>1</Option>
                <Option value={2}>2</Option>
                <Option value={3}>3</Option>
                <Option value={5}>5</Option>
                <Option value={8}>8</Option>
                <Option value={13}>13</Option>
                <Option value={21}>21</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="assigneeId"
              label="负责人"
            >
              <Select
                className="bg-gray-700/50 border-gray-600"
                placeholder={projectMembers.length > 0 ? "选择负责人" : "当前项目暂无成员"}
                allowClear
                disabled={projectMembers.length === 0}
                showSearch
                filterOption={(input, option) =>
                  String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
              >
                {projectMembers.length > 0 ? (
                  projectMembers.map((member) => (
                    <Option key={member.user.id} value={member.user.id} label={`${member.user.username} (${member.user.email})`}>
                      {member.user.username} ({member.user.email})
                    </Option>
                  ))
                ) : (
                  <Option value="" disabled>请先选择项目</Option>
                )}
              </Select>
            </Form.Item>
          </div>

          <Form.Item
            name="acceptanceCriteria"
            label="验收标准"
          >
            <TextArea
              rows={3}
              placeholder="描述用户故事的验收标准..."
              className="bg-gray-700/50 border-gray-600 text-white"
              showCount
              maxLength={2000}
            />
          </Form.Item>

          <Form.Item className="pt-4 mb-0">
            <div className="flex gap-4">
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="flex-1 bg-gradient-to-r from-orange-500 to-orange-600 border-none"
              >
                {editingStory ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setFormModalOpen(false);
                  form.resetFields();
                }}
                className="flex-1 border-gray-600 text-gray-300"
              >
                取消
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Modal>

      {/* 详情抽屉 */}
      <Drawer
        title="用户故事详情"
        placement="right"
        size="large"
        open={detailDrawerOpen}
        onClose={() => {
          setDetailDrawerOpen(false);
          setSelectedStory(null);
        }}
        className="glass-dark"
      >
        {selectedStory && (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">{selectedStory.title}</h3>
              <div className="flex items-center gap-2 flex-wrap">
                <Tag color={statusColorMap[selectedStory.status]}>
                  {statusTextMap[selectedStory.status]}
                </Tag>
                <Tag color={priorityColorMap[selectedStory.priority]}>
                  {priorityTextMap[selectedStory.priority]}
                </Tag>
                {selectedStory.storyPoints && (
                  <Tag color="orange">{selectedStory.storyPoints} 故事点</Tag>
                )}
              </div>
            </div>

            <div>
              <h4 className="text-sm font-medium text-gray-400 mb-2">描述</h4>
              <p className="text-gray-300 whitespace-pre-wrap">{selectedStory.description || '无描述'}</p>
            </div>

            {selectedStory.acceptanceCriteria && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">验收标准</h4>
                <p className="text-gray-300 whitespace-pre-wrap">{selectedStory.acceptanceCriteria}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">负责人</h4>
                <div className="flex items-center gap-2">
                  <Avatar
                    size={24}
                    className="bg-gradient-to-br from-purple-400 to-pink-500"
                  >
                    {selectedStory.assigneeId ? (selectedStory.assigneeName?.charAt(0).toUpperCase() || 'U') : '?'}
                  </Avatar>
                  <span className="text-gray-300">
                    {selectedStory.assigneeId ? (selectedStory.assigneeName || `用户 ${selectedStory.assigneeId}`) : '未分配'}
                  </span>
                </div>
              </div>
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">项目</h4>
                <div className="flex items-center gap-2">
                  {getProject(selectedStory.projectId) && (
                    <>
                      <span>{getProject(selectedStory.projectId)?.icon}</span>
                      <span className="text-gray-300">{getProject(selectedStory.projectId)?.name}</span>
                    </>
                  )}
                </div>
              </div>
            </div>

            <div>
              <h4 className="text-sm font-medium text-gray-400 mb-2">时间信息</h4>
              <div className="text-gray-300 text-sm">
                <div>创建：{new Date(selectedStory.createdAt).toLocaleString('zh-CN')}</div>
                {selectedStory.updatedAt && (
                  <div>更新：{new Date(selectedStory.updatedAt).toLocaleString('zh-CN')}</div>
                )}
              </div>
            </div>
          </div>
        )}
      </Drawer>
    </div>
  );
}
