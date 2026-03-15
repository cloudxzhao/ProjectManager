'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, Pagination, Modal, Form, Drawer, message, FormProps } from 'antd';
import { FileTextOutlined, PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { searchStories, deleteStory, createStory, updateStory, type UserStory, type StoryStatus, type Priority, type CreateUserStoryDto, type UpdateUserStoryDto, statusTextMap, priorityTextMap, statusMap, priorityMap } from '@/lib/api/story';
import { getAuthorizedProjects, getProjectMembers } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';
import type { ProjectMemberResponse } from '@/lib/api/project';

const { Option } = Select;
const { TextArea } = Input;

// 状态颜色映射（支持大写和小写）
const statusColorMap: Record<string, string> = {
  TODO: 'default',
  IN_PROGRESS: 'processing',
  IN_REVIEW: 'warning',
  DONE: 'success',
  todo: 'default',
  in_progress: 'processing',
  in_review: 'warning',
  done: 'success',
};

// 状态文本映射（支持大写和小写）
const statusTextLabelMap: Record<string, string> = {
  TODO: '待办',
  IN_PROGRESS: '开发中',
  IN_REVIEW: '测试中',
  DONE: '已完成',
  todo: '待办',
  in_progress: '开发中',
  in_review: '测试中',
  done: '已完成',
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

// 用户故事图标库（用于随机展示）
const storyIcons = ['📝', '💭', '🎯', '✨', '🔥', '💡', '🚀', '⭐', '🎨', '🧩', '📌', '💫', '🌟', '🎪', '🎭', '🎸'];

// 根据故事 ID 生成稳定的随机索引（保证同一条故事每次渲染显示相同图标）
const getStableIconIndex = (id: number) => {
  const hash = String(id).split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return hash % storyIcons.length;
};

interface StoryCardProps {
  story: UserStory;
  project?: Project;
  onView: (story: UserStory) => void;
  onEdit: (story: UserStory) => void;
  onDelete: (story: UserStory) => void;
}

// 用户故事卡片组件
const StoryCard: React.FC<StoryCardProps> = ({ story, project, onView, onEdit, onDelete }) => {
  // 获取稳定的图标（基于故事 ID）
  const storyIcon = storyIcons[getStableIconIndex(story.id)];

  return (
    <Card
      hoverable
      className="h-full transition-all duration-300 group"
      style={{
        background: 'linear-gradient(145deg, #1e2230, #161922)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '16px',
        padding: '20px',
        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.5)',
      }}
      bodyStyle={{ padding: 0 }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-5px)';
        e.currentTarget.style.boxShadow = '0 15px 40px rgba(0, 0, 0, 0.6)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.15)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.5)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
      }}
    >
      <div className="p-5">
        {/* 头部：图标 + 状态 */}
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2.5">
            {/* 项目图标 */}
            <div
              className="w-9 h-9 flex items-center justify-center text-lg"
              style={{
                background: `linear-gradient(135deg, #3a8dff, #0052d4)`,
                borderRadius: '10px',
                boxShadow: '0 4px 12px rgba(58, 141, 255, 0.3)',
              }}
            >
              {storyIcon}
            </div>
            {/* 状态标签 */}
            <span
              className="px-2.5 py-1 rounded-md text-xs font-medium"
              style={{
                background: 'rgba(255, 255, 255, 0.1)',
                color: '#a0a6b5',
              }}
            >
              {statusTextLabelMap[story.status]}
            </span>
          </div>
          {/* 优先级标签 */}
          <span
            className="px-2 py-1 rounded-md text-xs"
            style={{
              background: `rgba(58, 141, 255, 0.1)`,
              color: '#3a8dff',
              border: '1px solid rgba(58, 141, 255, 0.3)',
            }}
          >
            {priorityTextLabelMap[story.priority]}
          </span>
        </div>

        {/* 内容区 */}
        <div className="mb-5">
          {/* 标题 */}
          <h3
            className="text-lg font-bold mb-1.5 line-clamp-2"
            style={{
              color: '#ffffff',
              letterSpacing: '0.5px',
            }}
          >
            {story.title}
          </h3>

          {/* 描述 */}
          <p
            className="text-sm mb-4 line-clamp-2"
            style={{
              color: '#808695',
              lineHeight: 1.5,
            }}
          >
            {story.description}
          </p>

          {/* 故事点 */}
          {story.storyPoints && (
            <div
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full"
              style={{
                background: 'rgba(255, 159, 67, 0.1)',
              }}
            >
              <span style={{ color: '#ff9f43', fontSize: '13px', fontWeight: 600 }}>
                ⚡ 故事点：{story.storyPoints} pts
              </span>
            </div>
          )}
        </div>

        {/* 分割线 */}
        <div
          className="mb-4"
          style={{
            height: '1px',
            background: 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent)',
          }}
        />

        {/* 底部操作栏 */}
        <div className="flex items-center justify-between">
          {/* 头像 */}
          <div
            className="w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm"
            style={{
              background: story.assigneeId ? 'linear-gradient(135deg, #3a8dff, #0052d4)' : '#4b5563',
              color: 'white',
              border: '2px solid #1e2230',
            }}
          >
            {story.assigneeId ? (story.assigneeName?.charAt(0).toUpperCase() || `U${story.assigneeId}`) : '?'}
          </div>

          {/* 操作按钮 */}
          <div className="flex items-center gap-4">
            <button
              onClick={() => onView(story)}
              className="text-lg cursor-pointer transition-colors duration-200"
              style={{ color: '#636e72' }}
              title="查看"
            >
              👁️
            </button>
            <button
              onClick={() => onEdit(story)}
              className="text-lg cursor-pointer transition-colors duration-200"
              style={{ color: '#636e72' }}
              title="编辑"
            >
              ✏️
            </button>
            <button
              onClick={() => onDelete(story)}
              className="text-lg cursor-pointer transition-colors duration-200"
              style={{ color: '#636e72' }}
              title="删除"
            >
              🗑️
            </button>
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
  const [stories, setStories] = useState<UserStory[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [allProjectMembers, setAllProjectMembers] = useState<Map<number, ProjectMemberResponse[]>>(new Map());  // 所有项目的成员缓存
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(10);
  const [selectedProjectIds, setSelectedProjectIds] = useState<number[]>([]);  // 支持多选
  const [selectedStatus, setSelectedStatus] = useState<StoryStatus | undefined>(undefined);
  const [selectedAssigneeId, setSelectedAssigneeId] = useState<number | undefined>(undefined);  // 责任人筛选
  const [selectedPriority, setSelectedPriority] = useState<Priority | undefined>(undefined);  // 优先级筛选
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

  // 当前表单中项目对应的成员列表（用于负责人选择）
  const [currentProjectMembers, setCurrentProjectMembers] = useState<ProjectMemberResponse[]>([]);

  // 加载用户有权限的项目列表
  const fetchProjects = async () => {
    try {
      const result = await getAuthorizedProjects();
      setProjects(result?.list || []);
    } catch (error) {
      console.error('加载项目列表失败:', error);
    }
  };

  // 加载项目成员列表（用于责任人筛选）
  const fetchProjectMembers = async (projectId: number) => {
    try {
      const members = await getProjectMembers(projectId);
      // 缓存到 Map 中
      setAllProjectMembers((prev) => new Map(prev).set(projectId, members || []));
    } catch (error) {
      console.error('加载项目成员失败:', error);
    }
  };

  // 加载所有选中项目的成员（用于责任人筛选下拉）
  const fetchSelectedProjectMembers = async (projectIds: number[]) => {
    if (projectIds.length === 0) return;

    // 并行加载所有项目的成员
    const promises = projectIds.map((id) => getProjectMembers(id));
    try {
      const results = await Promise.all(promises);
      const newMembersMap = new Map(allProjectMembers);
      projectIds.forEach((id, index) => {
        newMembersMap.set(id, results[index] || []);
      });
      setAllProjectMembers(newMembersMap);
    } catch (error) {
      console.error('批量加载项目成员失败:', error);
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
      if (selectedAssigneeId) params.assigneeId = selectedAssigneeId;  // 责任人筛选
      if (selectedPriority) params.priority = selectedPriority;  // 优先级筛选
      if (searchText) params.keyword = searchText;
      // 如果选择了项目，通过 projectIds 参数筛选（支持多选）
      if (selectedProjectIds && selectedProjectIds.length > 0) {
        params.projectIds = selectedProjectIds;
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

  // 当选中项目变化时，加载成员列表
  useEffect(() => {
    if (selectedProjectIds.length > 0) {
      fetchSelectedProjectMembers(selectedProjectIds);
    }
  }, [selectedProjectIds]);

  useEffect(() => {
    fetchStories();
  }, [page, selectedProjectIds, selectedStatus, selectedAssigneeId, selectedPriority, searchText]);

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
    // 默认选择当前筛选的项目（如果只选了一个）
    if (selectedProjectIds.length === 1) {
      form.setFieldsValue({ projectId: selectedProjectIds[0] });
      // 加载项目成员列表
      const projectId = selectedProjectIds[0];
      const members = allProjectMembers.get(projectId) || [];
      setCurrentProjectMembers(members);
    } else {
      // 如果没有选择项目或选了多个，清空表单中的项目字段
      form.setFieldsValue({ projectId: undefined });
      setCurrentProjectMembers([]);
    }
    setFormModalOpen(true);
  };

  // 打开编辑表单
  const handleEdit = (story: UserStory) => {
    setEditingStory(story);
    // 编辑时加载对应项目的成员列表（用于负责人选择下拉）
    const members = allProjectMembers.get(story.projectId) || [];
    setCurrentProjectMembers(members);
    if (members.length === 0) {
      fetchProjectMembers(story.projectId).then(() => {
        const updatedMembers = allProjectMembers.get(story.projectId) || [];
        setCurrentProjectMembers(updatedMembers);
      });
    }
    form.setFieldsValue({
      projectId: story.projectId,
      title: story.title,
      description: story.description,
      acceptanceCriteria: story.acceptanceCriteria,
      status: story.status,  // 直接使用后端返回的状态值
      priority: story.priority?.toLowerCase(),  // 转小写用于表单（优先级 Option 是小写）
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
        // 需要使用 selectedProjectIds 的第一个或 values.projectId
        const targetProjectId = selectedProjectIds.length === 1 ? selectedProjectIds[0] : values.projectId;
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

  // 获取所有选中项目的成员（用于责任人筛选下拉）
  const getFilterMembers = () => {
    if (selectedProjectIds.length === 0) return [];
    const members: ProjectMemberResponse[] = [];
    const seenIds = new Set<number>();
    selectedProjectIds.forEach((projectId) => {
      const projectMembers = allProjectMembers.get(projectId) || [];
      projectMembers.forEach((m) => {
        if (!seenIds.has(m.userId)) {
          seenIds.add(m.userId);
          members.push(m);
        }
      });
    });
    return members;
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
            mode="multiple"
            placeholder="选择项目（可多选）"
            value={selectedProjectIds}
            onChange={(value) => {
              setSelectedProjectIds(value);
              setSelectedAssigneeId(undefined);  // 清空责任人筛选
              setPage(1);
            }}
            className="w-[250px] bg-gray-700/50 border-gray-600"
            allowClear
            maxTagCount="responsive"
            optionLabelProp="label"
          >
            {projects.map((project) => (
              <Option key={project.id} value={project.id} label={project.name}>
                <div className="flex items-center gap-2">
                  <span>{project.icon || '📁'}</span>
                  <span>{project.name}</span>
                </div>
              </Option>
            ))}
          </Select>
          <Select
            placeholder="选择责任人"
            value={selectedAssigneeId}
            onChange={(value) => {
              setSelectedAssigneeId(value);
              setPage(1);
            }}
            className="w-[200px] bg-gray-700/50 border-gray-600"
            allowClear
            showSearch
            filterOption={(input, option) =>
              String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
            options={getFilterMembers().map((member) => ({
              label: member.username,
              value: member.userId,
            }))}
            disabled={selectedProjectIds.length === 0}
          />
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
            <Option value="TODO">待办</Option>
            <Option value="IN_PROGRESS">开发中</Option>
            <Option value="IN_REVIEW">测试中</Option>
            <Option value="DONE">已完成</Option>
          </Select>
          <Select
            placeholder="选择优先级"
            value={selectedPriority}
            onChange={(value) => {
              setSelectedPriority(value);
              setPage(1);
            }}
            className="w-[150px] bg-gray-700/50 border-gray-600"
            allowClear
          >
            <Option value="low">低</Option>
            <Option value="medium">中</Option>
            <Option value="high">高</Option>
            <Option value="urgent">紧急</Option>
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
                onDelete={(story) => {
                  setDeletingStory(story);
                  setDeleteModalOpen(true);
                }}
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
                  const members = allProjectMembers.get(value) || [];
                  if (members.length > 0) {
                    setCurrentProjectMembers(members);
                  } else {
                    fetchProjectMembers(value).then(() => {
                      const updatedMembers = allProjectMembers.get(value) || [];
                      setCurrentProjectMembers(updatedMembers);
                    });
                  }
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
                  <Option value="TODO">待办</Option>
                  <Option value="IN_PROGRESS">开发中</Option>
                  <Option value="IN_REVIEW">测试中</Option>
                  <Option value="DONE">已完成</Option>
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
                placeholder={currentProjectMembers.length > 0 ? "选择负责人" : "当前项目暂无成员"}
                allowClear
                disabled={currentProjectMembers.length === 0 && !editingStory?.assigneeId}
                showSearch
                filterOption={(input, option) =>
                  String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
              >
                {currentProjectMembers.length > 0 ? (
                  currentProjectMembers.map((member) => (
                    <Option key={member.userId} value={member.userId} label={member.username}>
                      {member.username}
                    </Option>
                  ))
                ) : (
                  // 编辑时如果成员列表为空，添加一个临时 Option 显示当前负责人
                  editingStory?.assigneeId && (
                    <Option key={editingStory.assigneeId} value={editingStory.assigneeId} label={editingStory.assigneeName || `用户 ${editingStory.assigneeId}`}>
                      {editingStory.assigneeName || `用户 ${editingStory.assigneeId}`}
                    </Option>
                  )
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
                  {statusTextLabelMap[selectedStory.status]}
                </Tag>
                <Tag color={priorityColorMap[selectedStory.priority]}>
                  {priorityTextLabelMap[selectedStory.priority]}
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
