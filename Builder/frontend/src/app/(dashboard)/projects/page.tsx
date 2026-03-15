'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, message, Pagination, Modal, Form, Drawer, ColorPicker, DatePicker } from 'antd';
import { PlusOutlined, SearchOutlined, AppstoreOutlined, UnorderedListOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getProjects, getProjectStats, updateProject, deleteProject, createProject } from '@/lib/api/project';
import type { Project, ProjectStatus, ProjectStats } from '@/types/project';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

// 统计数据结构
interface StatData {
  label: string;
  value: number;
  color: string;
  gradient: string;
}

const statusColorMap: Record<string, string> = {
  ACTIVE: 'processing',
  COMPLETED: 'success',
  ARCHIVED: 'default',
  PLANNING: 'default',
};

const statusTextMap: Record<string, string> = {
  ACTIVE: '进行中',
  COMPLETED: '已完成',
  ARCHIVED: '已归档',
  PLANNING: '规划中',
};

interface ProjectCardProps {
  project: Project;
  index: number;
  onView: (project: Project) => void;
  onEdit: (project: Project) => void;
  onDelete: (project: Project) => void;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project, index, onView, onEdit, onDelete }) => {
  // 计算进度百分比
  const calculateProgress = () => {
    if (project.taskCount === 0) return 0;
    return Math.round((project.completedTaskCount / project.taskCount) * 100);
  };

  const progress = calculateProgress();

  return (
    <Card
      hoverable
      className="h-full transition-all duration-300 group cursor-pointer"
      style={{
        background: 'linear-gradient(145deg, #1e2230, #161922)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '16px',
        padding: '0',
        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.5)',
      }}
      bodyStyle={{ padding: 0 }}
      onClick={() => onView(project)}
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
        {/* 项目图标和状态 */}
        <div className="flex items-start justify-between mb-3">
          <div
            className="w-12 h-12 rounded-xl flex items-center justify-center text-xl shadow-lg"
            style={{
              background: `linear-gradient(135deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
              boxShadow: `0 6px 12px -4px ${project.color || '#f97316'}40`,
            }}
          >
            {project.icon || '📁'}
          </div>
          <Tag
            color={statusColorMap[project.status] as any}
            className="text-xs font-bold uppercase tracking-wider"
          >
            {statusTextMap[project.status]}
          </Tag>
        </div>

        {/* 项目名称和描述 */}
        <h3 className="text-base font-semibold text-white mb-2 font-display truncate">{project.name}</h3>
        <p className="text-sm text-gray-400 line-clamp-2 mb-3 h-9">
          {project.description}
        </p>

        {/* 项目信息网格 */}
        <div className="grid grid-cols-2 gap-3 pt-3 border-t border-white/10 mb-3">
          <div className="flex flex-col gap-1">
            <span className="text-xs text-gray-500 uppercase tracking-wider">成员</span>
            <span className="text-sm text-white font-medium flex items-center gap-1">
              <span>👥</span> {project.memberCount} 人
            </span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-gray-500 uppercase tracking-wider">任务</span>
            <span className="text-sm text-white font-medium flex items-center gap-1">
              <span>📋</span> {project.taskCount} 个
            </span>
          </div>
        </div>
      </div>

      {/* 底部操作栏 */}
      <div className="px-5 py-3 border-t border-white/10 flex items-center justify-between" style={{ background: 'rgba(0,0,0,0.2)' }}>
        <div className="flex -space-x-2">
          {Array.from({ length: Math.min(project.memberCount || 1, 4) }).map((_, i) => (
            <Avatar
              key={i}
              size={24}
              className="border-2"
              style={{
                backgroundColor: `${project.color || '#f97316'}40`,
                borderColor: '#1e2230',
                marginLeft: i > 0 ? '-8px' : 0
              }}
              icon={<span className="text-xs">U{i + 1}</span>}
            />
          ))}
          {(project.memberCount || 0) > 4 && (
            <Avatar
              size={24}
              className="border-2 border-dashed"
              style={{
                backgroundColor: 'rgba(255,255,255,0.05)',
                borderColor: '#1e2230',
                marginLeft: '-8px'
              }}
              icon={<span className="text-xs text-gray-400">+{(project.memberCount || 0) - 4}</span>}
            />
          )}
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={(e) => { e.stopPropagation(); onView(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="查看"
          >
            👁️
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onEdit(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="编辑"
          >
            ✏️
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onDelete(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="删除"
          >
            🗑️
          </button>
        </div>
      </div>
    </Card>
  );
};

// 列表视图项目组件
const ProjectListItem: React.FC<ProjectCardProps> = ({ project, index, onView, onEdit, onDelete }) => {
  // 计算进度百分比
  const calculateProgress = () => {
    if (project.taskCount === 0) return 0;
    return Math.round((project.completedTaskCount / project.taskCount) * 100);
  };

  const progress = calculateProgress();

  return (
    <Card
      hoverable
      className="transition-all duration-300 hover:bg-white/5 group cursor-pointer"
      onClick={() => onView(project)}
      style={{
        background: 'linear-gradient(145deg, #1e2230, #161922)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '16px',
        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.5)',
      }}
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
      <div className="flex items-center gap-4 py-4 px-5">
        {/* 项目图标 */}
        <div
          className="w-12 h-12 rounded-xl flex items-center justify-center text-xl flex-shrink-0"
          style={{
            background: `linear-gradient(135deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
            boxShadow: `0 6px 12px -4px ${project.color || '#f97316'}40`,
          }}
        >
          {project.icon || '📁'}
        </div>

        {/* 项目信息 */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-3 mb-1">
            <h3 className="text-base font-semibold text-white font-display truncate">{project.name}</h3>
            <Tag
              color={statusColorMap[project.status] as any}
              className="text-xs font-bold uppercase tracking-wider flex-shrink-0"
            >
              {statusTextMap[project.status]}
            </Tag>
          </div>
          <p className="text-sm text-gray-400 truncate">{project.description}</p>
        </div>

        {/* 成员和任务数 */}
        <div className="flex items-center gap-6 flex-shrink-0">
          <div className="flex items-center gap-2">
            <span className="text-xs text-gray-500 uppercase">👥</span>
            <span className="text-sm text-white font-medium">{project.memberCount} 人</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-gray-500 uppercase">📋</span>
            <span className="text-sm text-white font-medium">{project.taskCount} 任务</span>
          </div>
        </div>

        {/* 进度条 */}
        <div className="w-32 flex-shrink-0">
          <div className="flex justify-between items-center mb-1">
            <span className="text-xs text-gray-400">{progress}%</span>
          </div>
          <div className="h-1.5 bg-white/10 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-700"
              style={{
                width: `${progress}%`,
                background: `linear-gradient(90deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
              }}
            />
          </div>
        </div>

        {/* 操作按钮 */}
        <div className="flex items-center gap-3 flex-shrink-0">
          <button
            onClick={(e) => { e.stopPropagation(); onView(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="查看"
          >
            👁️
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onEdit(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="编辑"
          >
            ✏️
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onDelete(project); }}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110"
            style={{ color: '#636e72' }}
            title="删除"
          >
            🗑️
          </button>
        </div>
      </div>
    </Card>
  );
};

// 统计卡片组件
const StatCard: React.FC<{ stat: StatData; index: number }> = ({ stat, index }) => (
  <div
    className="p-6 rounded-2xl transition-all duration-300 hover:-translate-y-0.5 animate-fade-in-up"
    style={{
      backgroundColor: 'rgba(255, 255, 255, 0.03)',
      borderColor: 'rgba(255, 255, 255, 0.08)',
      borderWidth: '1px',
      borderStyle: 'solid',
      animationDelay: `${index * 0.1}s`,
    }}
  >
    <div className="flex items-center gap-4">
      <div
        className="w-12 h-12 rounded-xl flex items-center justify-center text-xl shadow-lg"
        style={{
          background: `linear-gradient(135deg, ${stat.color} 0%, ${stat.color}cc 100%)`,
          boxShadow: `0 6px 12px -4px ${stat.color}40`,
        }}
      >
        {stat.label === '进行中' && '📁'}
        {stat.label === '已完成' && '✓'}
        {stat.label === '已归档' && '📦'}
      </div>
      <div>
        <div className="text-3xl font-bold text-white font-display">{stat.value}</div>
        <div className="text-sm text-gray-400">{stat.label}</div>
      </div>
    </div>
  </div>
);

export default function ProjectsPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('all');
  const [searchValue, setSearchValue] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [sortField, setSortField] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState('desc');

  // API 状态
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState<ProjectStats>({
    activeCount: 0,
    completedCount: 0,
    archivedCount: 0,
    planningCount: 0,
  });

  // 分页状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  // 编辑项目状态
  const [editDrawerVisible, setEditDrawerVisible] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [editForm] = Form.useForm();
  const [selectedEditIcon, setSelectedEditIcon] = useState('📁');
  const [selectedEditColor, setSelectedEditColor] = useState('#f97316');

  // 删除确认状态
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [deletingProject, setDeletingProject] = useState<Project | null>(null);

  // 创建项目抽屉状态
  const [createDrawerVisible, setCreateDrawerVisible] = useState(false);
  const [createProjectForm] = Form.useForm();
  const [selectedIcon, setSelectedIcon] = useState('📁');
  const [selectedColor, setSelectedColor] = useState('#f97316');

  // 统计卡片数据
  const statsData: StatData[] = [
    { label: '进行中', value: stats.activeCount, color: '#10b981', gradient: 'from-emerald-500 to-emerald-600' },
    { label: '已完成', value: stats.completedCount, color: '#3b82f6', gradient: 'from-blue-500 to-blue-600' },
    { label: '已归档', value: stats.archivedCount, color: '#64748b', gradient: 'from-slate-500 to-slate-600' },
  ];

  // 项目图标列表
  const projectIcons = ['📁', '🛒', '📱', '📊', '🤝', '🌐', '🔧', '💼', '🎯', '🚀', '💡'];

  // 打开创建项目抽屉
  const handleCreateProject = () => {
    createProjectForm.resetFields();
    setSelectedIcon('📁');
    setSelectedColor('#f97316');
    setCreateDrawerVisible(true);
  };

  // 创建项目提交
  const handleCreateProjectSubmit = async (values: any) => {
    try {
      const payload = {
        name: values.name,
        description: values.description,
        startDate: values.startDate?.format('YYYY-MM-DD') || '',
        endDate: values.endDate?.format('YYYY-MM-DD') || values.startDate?.format('YYYY-MM-DD') || '',
        color: selectedColor,
        icon: selectedIcon,
      };
      await createProject(payload);
      message.success('项目创建成功');
      setCreateDrawerVisible(false);
      fetchProjects();
      fetchStats();
    } catch (error) {
      console.error('创建项目失败:', error);
      message.error('创建失败');
    }
  };

  // 查看项目详情
  const handleViewProject = (project: Project) => {
    router.push(`/projects/${project.id}`);
  };

  // 编辑项目
  const handleEditProject = (project: Project) => {
    setEditingProject(project);
    setSelectedEditIcon(project.icon || '📁');
    setSelectedEditColor(project.color || '#f97316');
    editForm.setFieldsValue({
      name: project.name,
      description: project.description,
      status: project.status,
      startDate: project.startDate ? dayjs(project.startDate) : undefined,
      endDate: project.endDate ? dayjs(project.endDate) : undefined,
      color: project.color,
      icon: project.icon,
    });
    setEditDrawerVisible(true);
  };

  // 保存编辑
  const handleEditSave = async (values: any) => {
    try {
      if (editingProject) {
        await updateProject(editingProject.id, {
          name: values.name,
          description: values.description,
          status: values.status,
          startDate: values.startDate?.format('YYYY-MM-DD') || '',
          endDate: values.endDate?.format('YYYY-MM-DD') || '',
          color: values.color || selectedEditColor,
          icon: values.icon || selectedEditIcon,
        });
        message.success('项目更新成功');
        setEditDrawerVisible(false);
        fetchProjects();
        fetchStats();
      }
    } catch (error) {
      console.error('保存失败:', error);
      message.error('保存失败');
    }
  };

  // 删除项目
  const handleDeleteProject = (project: Project) => {
    setDeletingProject(project);
    setDeleteModalVisible(true);
  };

  // 确认删除
  const handleDeleteConfirm = async () => {
    if (deletingProject) {
      try {
        await deleteProject(deletingProject.id);
        message.success('项目已删除');
        setDeleteModalVisible(false);
        fetchProjects();
        fetchStats();
      } catch (error) {
        console.error('删除失败:', error);
        message.error('删除失败');
      }
    }
  };

  // 获取统计信息
  const fetchStats = async () => {
    try {
      const result = await getProjectStats();
      setStats(result);
    } catch (error) {
      console.error('获取统计信息失败:', error);
    }
  };

  // 获取项目列表
  const fetchProjects = async () => {
    setLoading(true);
    try {
      // 根据选中 tab 确定状态过滤
      let status: ProjectStatus | undefined = undefined;
      if (activeTab === 'active') status = 'ACTIVE';
      else if (activeTab === 'completed') status = 'COMPLETED';
      else if (activeTab === 'archived') status = 'ARCHIVED';

      const result = await getProjects(currentPage, pageSize, searchValue || undefined, status, sortField, sortOrder);
      console.log('[ProjectsPage] API result:', result);

      const projectList: Project[] = result.list || [];
      setProjects(projectList);
      setTotal(result.total || 0);
    } catch (error) {
      console.error('获取项目列表失败:', error);
      message.error('获取项目列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 初始化加载
  useEffect(() => {
    fetchStats();
    fetchProjects();
  }, [currentPage, pageSize, activeTab, sortField, sortOrder]);

  // 搜索（防抖）
  useEffect(() => {
    const timer = setTimeout(() => {
      setCurrentPage(1); // 搜索时重置到第一页
      fetchProjects();
    }, 500);
    return () => clearTimeout(timer);
  }, [searchValue]);

  const tabItems = [
    { key: 'all', label: '全部', count: total },
    { key: 'active', label: '进行中', count: stats.activeCount },
    { key: 'completed', label: '已完成', count: stats.completedCount },
    { key: 'archived', label: '已归档', count: stats.archivedCount },
  ];

  return (
    <div className="space-y-8 p-8">
      {/* 页面标题 */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white font-display">我的项目</h1>
          <p className="text-gray-400 mt-1">管理和追踪您所有的项目</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={handleCreateProject}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none shadow-lg hover:shadow-orange-500/30 transition-all hover:-translate-y-0.5"
        >
          新建项目
        </Button>
      </div>

      {/* 统计概览 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {statsData.map((stat, index) => (
          <StatCard key={stat.label} stat={stat} index={index} />
        ))}
      </div>

      {/* 筛选工具栏 */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-2 p-1 rounded-xl" style={{ backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)' }}>
          {tabItems.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`px-5 py-2.5 rounded-lg text-sm font-medium transition-all relative ${
                activeTab === tab.key
                  ? 'bg-primary text-white'
                  : 'text-gray-400 hover:text-white hover:bg-white/5'
              }`}
            >
              {tab.label}
              <span
                className={`ml-2 px-2 py-0.5 rounded-full text-xs ${
                  activeTab === tab.key ? 'bg-orange-500 text-white' : 'bg-white/10 text-gray-400'
                }`}
              >
                {tab.count}
              </span>
              {activeTab === tab.key && (
                <span
                  className="absolute -bottom-1 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full"
                  style={{
                    backgroundColor: tab.key === 'active' ? '#10b981' : tab.key === 'completed' ? '#3b82f6' : '#94a3b8',
                  }}
                />
              )}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-3">
          {/* 排序下拉框 */}
          <Select
            value={sortField}
            onChange={(value) => {
              setSortField(value);
              setCurrentPage(1); // 重置到第一页
            }}
            className="w-36"
            style={{ backgroundColor: 'rgba(255,255,255,0.03)' }}
            options={[
              { value: 'createdAt', label: '创建时间' },
              { value: 'name', label: '项目名称' },
              { value: 'startDate', label: '开始日期' },
              { value: 'endDate', label: '结束日期' },
            ]}
          />
          {/* 排序方向切换按钮 */}
          <Button
            onClick={() => {
              setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
              setCurrentPage(1); // 重置到第一页
            }}
            className="w-10 h-10 flex items-center justify-center"
            style={{ backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)' }}
            title={sortOrder === 'asc' ? '升序' : '降序'}
          >
            {sortOrder === 'asc' ? (
              <span className="text-orange-500 text-lg">↑</span>
            ) : (
              <span className="text-orange-500 text-lg">↓</span>
            )}
          </Button>
          <div className="flex p-1 rounded-lg" style={{ backgroundColor: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)' }}>
            <button
              onClick={() => setViewMode('grid')}
              className={`p-2 rounded-md transition-all ${viewMode === 'grid' ? 'bg-orange-500 text-white' : 'text-gray-400 hover:text-white'}`}
            >
              <AppstoreOutlined />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`p-2 rounded-md transition-all ${viewMode === 'list' ? 'bg-orange-500 text-white' : 'text-gray-400 hover:text-white'}`}
            >
              <UnorderedListOutlined />
            </button>
          </div>
        </div>
      </div>

      {/* 搜索框 */}
      <div className="flex items-center gap-4">
        <div className="flex-1 relative">
          <Input
            placeholder="搜索项目名称或描述..."
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onPressEnter={() => fetchProjects()}
            className="bg-white/5 border-white/10 text-white placeholder-gray-500 focus:border-orange-500 focus:shadow-[0_0_0_3px_rgba(249,115,22,0.2)]"
            allowClear
            suffix={<SearchOutlined className="text-orange-500" />}
          />
        </div>
      </div>

      {/* 项目列表 */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" description="加载中..." />
        </div>
      ) : projects.length > 0 ? (
        <>
          {viewMode === 'grid' ? (
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              {projects.map((project, index) => (
                <div
                  key={project.id}
                  className="animate-fade-in-up"
                  style={{ animationDelay: `${index * 0.05}s` }}
                >
                  <ProjectCard
                    project={project}
                    index={index}
                    onView={handleViewProject}
                    onEdit={handleEditProject}
                    onDelete={handleDeleteProject}
                  />
                </div>
              ))}
            </div>
          ) : (
            <div className="space-y-4">
              {projects.map((project, index) => (
                <div
                  key={project.id}
                  className="animate-fade-in-up"
                  style={{ animationDelay: `${index * 0.05}s` }}
                >
                  <ProjectListItem
                    project={project}
                    index={index}
                    onView={handleViewProject}
                    onEdit={handleEditProject}
                    onDelete={handleDeleteProject}
                  />
                </div>
              ))}
            </div>
          )}

          {/* 分页组件 */}
          <div className="flex justify-center mt-8">
            <Pagination
              current={currentPage}
              pageSize={pageSize}
              total={total}
              onChange={(page, size) => {
                setCurrentPage(page);
                if (size) setPageSize(size);
              }}
              showSizeChanger
              showTotal={(total) => `共 ${total} 个项目`}
              pageSizeOptions={['10', '30', '50', '100']}
              className="text-white"
            />
          </div>
        </>
      ) : (
        <Card
          className="text-center"
          style={{ backgroundColor: 'rgba(255,255,255,0.03)', borderColor: 'rgba(255,255,255,0.08)' }}
        >
          <Empty
            description={
              <span className="text-gray-400">{searchValue ? '未找到匹配的项目' : '暂无项目'}</span>
            }
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
          {!searchValue && (
            <div className="mt-4">
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateProject} className="bg-orange-500 border-none">
                创建第一个项目
              </Button>
            </div>
          )}
        </Card>
      )}

      {/* 编辑项目抽屉 - 左侧边栏式 */}
      <Drawer
        title="编辑项目"
        placement="left"
        open={editDrawerVisible}
        onClose={() => setEditDrawerVisible(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setEditDrawerVisible(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => editForm.submit()}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              保存修改
            </Button>
          </div>
        }
      >
        <div style={{ padding: '24px', height: '100%', overflowY: 'auto' }}>
          <Form
            form={editForm}
            layout="vertical"
            onFinish={handleEditSave}
            size="large"
          >
            <Form.Item
              name="name"
              label="项目名称"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[
                { required: true, message: '请输入项目名称' },
                { min: 2, message: '项目名称至少 2 个字符' },
                { max: 50, message: '项目名称不能超过 50 个字符' },
              ]}
            >
              <Input
                placeholder="例如：电商平台重构"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="项目描述"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入项目描述' }]}
            >
              <TextArea
                rows={10}
                placeholder="描述项目目标、范围等..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '200px',
                }}
                showCount
                maxLength={500}
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="startDate"
                label="开始日期"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <DatePicker
                  style={{
                    width: '100%',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  format="YYYY-MM-DD"
                  placeholder="选择开始日期"
                />
              </Form.Item>

              <Form.Item
                name="endDate"
                label="结束日期"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <DatePicker
                  style={{
                    width: '100%',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  format="YYYY-MM-DD"
                  placeholder="选择结束日期"
                />
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="status"
                label="项目状态"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <Select
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  options={[
                    { value: 'ACTIVE', label: '进行中' },
                    { value: 'COMPLETED', label: '已完成' },
                    { value: 'ARCHIVED', label: '已归档' },
                    { value: 'PLANNING', label: '规划中' },
                  ]}
                />
              </Form.Item>

              <Form.Item
                name="color"
                label="项目颜色"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <ColorPicker
                  format="hex"
                  showText
                  className="w-full"
                  value={selectedEditColor}
                  onChange={(color) => {
                    const hexColor = color.toHexString();
                    setSelectedEditColor(hexColor);
                  }}
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                  }}
                />
              </Form.Item>
            </div>

            <Form.Item
              name="icon"
              label="项目图标"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <div className="flex gap-2 flex-wrap">
                {projectIcons.map((icon) => (
                  <button
                    key={icon}
                    type="button"
                    onClick={() => setSelectedEditIcon(icon)}
                    className={`w-12 h-12 text-2xl rounded-lg flex items-center justify-center transition-all ${
                      selectedEditIcon === icon
                        ? 'bg-orange-500 ring-2 ring-orange-400'
                        : 'bg-gray-700/50 hover:bg-gray-600'
                    }`}
                    style={{
                      background: selectedEditIcon === icon ? '#ff8c42' : 'rgba(255,255,255,0.05)',
                      border: selectedEditIcon === icon ? '2px solid #ff8c42' : '1px solid rgba(255,255,255,0.1)',
                    }}
                  >
                    {icon}
                  </button>
                ))}
              </div>
            </Form.Item>
          </Form>
        </div>
      </Drawer>

      {/* 删除确认弹窗 */}
      <Modal
        title="删除项目"
        open={deleteModalVisible}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalVisible(false)}
        width={400}
        okType="danger"
        okText="删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
        bodyStyle={{
          background: 'linear-gradient(145deg, #1e2230, #161922)',
          borderRadius: '16px',
          border: '1px solid rgba(255, 255, 255, 0.08)',
        }}
      >
        <div className="py-4">
          <p className="text-gray-300">
            确定要删除项目 <span className="text-orange-400 font-semibold">{deletingProject?.name}</span> 吗？
          </p>
          <p className="text-gray-500 text-sm mt-2">
            此操作不可恢复，项目下的所有任务和数据也将被删除。
          </p>
        </div>
      </Modal>

      {/* 创建项目抽屉 - 左侧边栏式 */}
      <Drawer
        title="创建项目"
        placement="left"
        open={createDrawerVisible}
        onClose={() => setCreateDrawerVisible(false)}
        width={600}
        styles={{
          body: { padding: 0, background: '#161b22', color: '#f0f6fc' },
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setCreateDrawerVisible(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => createProjectForm.submit()}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              创建项目
            </Button>
          </div>
        }
      >
        <div style={{ padding: '24px', height: '100%', overflowY: 'auto' }}>
          <Form
            form={createProjectForm}
            layout="vertical"
            onFinish={handleCreateProjectSubmit}
            size="large"
          >
            <Form.Item
              name="name"
              label="项目名称"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[
                { required: true, message: '请输入项目名称' },
                { min: 2, message: '项目名称至少 2 个字符' },
                { max: 50, message: '项目名称不能超过 50 个字符' },
              ]}
            >
              <Input
                placeholder="例如：电商平台重构"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="description"
              label="项目描述"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入项目描述' }]}
            >
              <TextArea
                rows={10}
                placeholder="描述项目目标、范围等..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '200px',
                }}
                showCount
                maxLength={500}
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-4">
              <Form.Item
                name="startDate"
                label="开始日期"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <DatePicker
                  style={{
                    width: '100%',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  format="YYYY-MM-DD"
                  placeholder="选择开始日期"
                />
              </Form.Item>

              <Form.Item
                name="endDate"
                label="结束日期"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <DatePicker
                  style={{
                    width: '100%',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                  }}
                  format="YYYY-MM-DD"
                  placeholder="选择结束日期"
                />
              </Form.Item>
            </div>

            <Form.Item
              name="icon"
              label="项目图标"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <div className="flex gap-2 flex-wrap">
                {projectIcons.map((icon) => (
                  <button
                    key={icon}
                    type="button"
                    onClick={() => setSelectedIcon(icon)}
                    className={`w-12 h-12 text-2xl rounded-lg flex items-center justify-center transition-all ${
                      selectedIcon === icon
                        ? 'bg-orange-500 ring-2 ring-orange-400'
                        : 'bg-gray-700/50 hover:bg-gray-600'
                    }`}
                    style={{
                      background: selectedIcon === icon ? '#ff8c42' : 'rgba(255,255,255,0.05)',
                      border: selectedIcon === icon ? '2px solid #ff8c42' : '1px solid rgba(255,255,255,0.1)',
                    }}
                  >
                    {icon}
                  </button>
                ))}
              </div>
            </Form.Item>

            <Form.Item
              name="color"
              label="项目颜色"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <ColorPicker
                format="hex"
                showText
                className="w-full"
                value={selectedColor}
                onChange={(color) => {
                  const hexColor = color.toHexString();
                  setSelectedColor(hexColor);
                }}
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                }}
              />
            </Form.Item>
          </Form>
        </div>
      </Drawer>
    </div>
  );
}
