'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, Pagination, Modal, Form, Drawer, message, FormProps } from 'antd';
import { FileTextOutlined, PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { getStories, deleteStory, createStory, updateStory, type UserStory, type StoryStatus, type Priority, type CreateUserStoryDto, type UpdateUserStoryDto } from '@/lib/api/story';
import { getProjects } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';

const { Option } = Select;
const { TextArea } = Input;

// 状态颜色映射
const statusColorMap: Record<string, string> = {
  todo: 'default',
  in_progress: 'processing',
  testing: 'warning',
  done: 'success',
};

// 状态文本映射
const statusTextMap: Record<string, string> = {
  todo: '待办',
  in_progress: '进行中',
  testing: '测试中',
  done: '已完成',
};

// 优先级颜色映射
const priorityColorMap: Record<string, string> = {
  low: 'gray',
  medium: 'blue',
  high: 'orange',
  urgent: 'red',
};

// 优先级文本映射
const priorityTextMap: Record<string, string> = {
  low: '低',
  medium: '中',
  high: '高',
  urgent: '紧急',
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
            <Tag color={statusColorMap[story.status]}>{statusTextMap[story.status]}</Tag>
          </div>
          <Tag color={priorityColorMap[story.priority]}>{priorityTextMap[story.priority]}</Tag>
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

        {/* 标签 */}
        {story.tags && story.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-4">
            {story.tags.map((tag) => (
              <Tag key={tag} color="blue" className="text-xs">{tag}</Tag>
            ))}
          </div>
        )}

        {/* 底部：负责人和操作 */}
        <div className="flex items-center justify-between pt-4 border-t border-gray-700">
          <Avatar
            size={28}
            className="bg-gradient-to-br from-purple-400 to-pink-500"
            icon={!story.assigneeId && <span className="text-xs">?</span>}
          >
            {story.assigneeId ? `U${story.assigneeId}` : ''}
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
  projectId: number;
  title: string;
  description?: string;
  status?: StoryStatus;
  priority?: Priority;
  assigneeId?: number;
  storyPoints?: number;
  dueDate?: string;
  tags?: string[];
}

export default function StoriesPage() {
  const [stories, setStories] = useState<UserStory[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
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
      const result = await getProjects({ page: 1, pageSize: 100 });
      setProjects(result?.list || []);
    } catch (error) {
      console.error('加载项目列表失败:', error);
    }
  };

  // 加载用户故事列表
  const fetchStories = async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = {
        page,
        pageSize,
      };
      if (selectedProject) params.projectId = selectedProject;
      if (selectedStatus) params.status = selectedStatus;
      if (searchText) params.title = searchText;

      const result = await getStories(selectedProject || 0, params);
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
    fetchStories();
  }, [page, selectedProject, selectedStatus]);

  // 删除用户故事
  const handleDelete = async () => {
    if (!deletingStory) return;
    setLoading(true);
    try {
      await deleteStory(deletingStory.projectId, deletingStory.id);
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
    }
    setFormModalOpen(true);
  };

  // 打开编辑表单
  const handleEdit = (story: UserStory) => {
    setEditingStory(story);
    form.setFieldsValue({
      projectId: story.projectId,
      title: story.title,
      description: story.description,
      status: story.status,
      priority: story.priority,
      assigneeId: story.assigneeId,
      storyPoints: story.storyPoints,
      dueDate: story.dueDate,
      tags: story.tags,
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
        // 更新
        const updateData: UpdateUserStoryDto = {
          title: values.title,
          description: values.description,
          status: values.status,
          priority: values.priority,
          assigneeId: values.assigneeId,
          storyPoints: values.storyPoints,
          dueDate: values.dueDate,
          tags: values.tags,
        };
        await updateStory(values.projectId, editingStory.id, updateData);
        message.success('用户故事更新成功');
      } else {
        // 创建
        const createData: CreateUserStoryDto = {
          projectId: values.projectId,
          title: values.title,
          description: values.description,
          status: values.status || 'todo',
          priority: values.priority || 'medium',
          assigneeId: values.assigneeId,
          storyPoints: values.storyPoints,
          dueDate: values.dueDate,
          tags: values.tags,
        };
        await createStory(values.projectId, createData);
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
              placeholder="搜索故事标题..."
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
            status: 'todo',
            priority: 'medium',
          }}
        >
          <Form.Item
            name="projectId"
            label="所属项目"
            rules={[{ required: true, message: '请选择所属项目' }]}
          >
            <Select className="bg-gray-700/50 border-gray-600" placeholder="选择项目">
              {projects.map((project) => (
                <Option key={project.id} value={project.id}>
                  {project.icon || '📁'} {project.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

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
              name="dueDate"
              label="截止日期"
            >
              <Input
                type="date"
                className="bg-gray-700/50 border-gray-600 text-white"
              />
            </Form.Item>
          </div>

          <Form.Item
            name="assigneeId"
            label="负责人 ID"
          >
            <Input
              type="number"
              placeholder="输入用户 ID"
              className="bg-gray-700/50 border-gray-600 text-white"
            />
          </Form.Item>

          <Form.Item
            name="tags"
            label="标签"
          >
            <Select
              mode="tags"
              placeholder="输入标签后按回车"
              className="bg-gray-700/50 border-gray-600"
              tokenSeparators={[',']}
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

            {selectedStory.tags && selectedStory.tags.length > 0 && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">标签</h4>
                <div className="flex flex-wrap gap-2">
                  {selectedStory.tags.map((tag) => (
                    <Tag key={tag} color="blue">{tag}</Tag>
                  ))}
                </div>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">负责人</h4>
                <div className="flex items-center gap-2">
                  <Avatar size={24} className="bg-gradient-to-br from-purple-400 to-pink-500">
                    {selectedStory.assigneeId ? `U${selectedStory.assigneeId}` : '?'}
                  </Avatar>
                  <span className="text-gray-300">
                    {selectedStory.assigneeId ? `用户 ${selectedStory.assigneeId}` : '未分配'}
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

            {selectedStory.dueDate && (
              <div>
                <h4 className="text-sm font-medium text-gray-400 mb-2">截止日期</h4>
                <span className="text-gray-300">
                  {new Date(selectedStory.dueDate).toLocaleDateString('zh-CN')}
                </span>
              </div>
            )}

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
