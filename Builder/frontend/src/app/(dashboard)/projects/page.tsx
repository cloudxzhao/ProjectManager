'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, message } from 'antd';
import { PlusOutlined, SearchOutlined, AppstoreOutlined, UnorderedListOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { getProjects } from '@/lib/api/project';
import type { Project, ProjectStatus } from '@/types/project';

const { Option } = Select;

// 统计数据结构
interface StatData {
  label: string;
  value: number;
  color: string;
  gradient: string;
}

const statusColorMap: Record<string, string> = {
  active: 'processing',
  completed: 'success',
  archived: 'default',
  planning: 'default',
};

const statusTextMap: Record<string, string> = {
  active: '进行中',
  completed: '已完成',
  archived: '已归档',
  planning: '规划中',
};

interface ProjectCardProps {
  project: Project;
  index: number;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project, index }) => {
  // 计算进度百分比
  const calculateProgress = () => {
    if (project.taskCount === 0) return 0;
    return Math.round((project.completedTaskCount / project.taskCount) * 100);
  };

  const progress = calculateProgress();

  return (
    <Link href={`/projects/${project.id}`}>
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
          className="h-1 w-full transition-opacity duration-300 group-hover:opacity-100"
          style={{
            background: `linear-gradient(90deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
            opacity: 0.8,
          }}
        />

        <div className="p-6">
          {/* 项目图标和状态 */}
          <div className="flex items-start justify-between mb-4">
            <div
              className="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl shadow-lg"
              style={{
                background: `linear-gradient(135deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
                boxShadow: `0 8px 16px -4px ${project.color || '#f97316'}40`,
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
          <h3 className="text-lg font-semibold text-white mb-2 font-display">{project.name}</h3>
          <p className="text-sm text-gray-400 line-clamp-2 mb-4 h-10">
            {project.description}
          </p>

          {/* 项目信息网格 */}
          <div className="grid grid-cols-2 gap-4 pt-4 border-t border-white/10">
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

        {/* 进度条区域 */}
        <div className="px-6 py-4 bg-black/20">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm text-gray-400">项目进度</span>
            <span className="text-sm font-mono font-semibold text-orange-400">{progress}%</span>
          </div>
          <div className="h-2 bg-white/10 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-700"
              style={{
                width: `${progress}%`,
                background: `linear-gradient(90deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
              }}
            />
          </div>
        </div>

        {/* 团队成员和查看按钮 */}
        <div className="px-6 py-4 flex items-center justify-between border-t border-white/5">
          <div className="flex items-center">
            <div className="flex -space-x-2">
              {Array.from({ length: Math.min(project.memberCount || 1, 4) }).map((_, i) => (
                <Avatar
                  key={i}
                  size={28}
                  className="border-2 border-primary"
                  style={{ backgroundColor: `${project.color || '#f97316'}40`, marginLeft: i > 0 ? '-8px' : 0 }}
                  icon={<span className="text-xs">U{i + 1}</span>}
                />
              ))}
              {(project.memberCount || 0) > 4 && (
                <Avatar
                  size={28}
                  className="border-2 border-primary border-dashed"
                  style={{ backgroundColor: 'rgba(255,255,255,0.05)', marginLeft: '-8px' }}
                  icon={<span className="text-xs text-gray-400">+{(project.memberCount || 0) - 4}</span>}
                />
              )}
            </div>
          </div>
          <span className="text-orange-400 text-sm font-semibold flex items-center gap-1 group-hover:gap-2 transition-all">
            查看详情 <span className="text-xs">→</span>
          </span>
        </div>
      </Card>
    </Link>
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
  const [activeTab, setActiveTab] = useState('all');
  const [searchValue, setSearchValue] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  // API 状态
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState<{ active: number; completed: number; archived: number }>({
    active: 0,
    completed: 0,
    archived: 0,
  });

  // 统计卡片数据
  const statsData: StatData[] = [
    { label: '进行中', value: stats.active, color: '#10b981', gradient: 'from-emerald-500 to-emerald-600' },
    { label: '已完成', value: stats.completed, color: '#3b82f6', gradient: 'from-blue-500 to-blue-600' },
    { label: '已归档', value: stats.archived, color: '#64748b', gradient: 'from-slate-500 to-slate-600' },
  ];

  // 获取项目列表
  const fetchProjects = async () => {
    setLoading(true);
    try {
      const result = await getProjects(1, 100, searchValue || undefined);
      console.log('[ProjectsPage] API result:', result);
      console.log('[ProjectsPage] result.list:', result.list);

      const projectList: Project[] = result.list || [];
      setProjects(projectList);

      // 计算统计数据
      setStats({
        active: projectList.filter((p: Project) => p.status === 'active').length,
        completed: projectList.filter((p: Project) => p.status === 'completed').length,
        archived: projectList.filter((p: Project) => p.status === 'archived').length,
      });
    } catch (error) {
      console.error('获取项目列表失败:', error);
      message.error('获取项目列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 初始化加载
  useEffect(() => {
    fetchProjects();
  }, []);

  // 筛选项目
  const filteredProjects = projects.filter((project) => {
    const matchesSearch =
      project.name.toLowerCase().includes(searchValue.toLowerCase()) ||
      project.description.toLowerCase().includes(searchValue.toLowerCase());

    if (activeTab === 'all') return matchesSearch;
    if (activeTab === 'active') return matchesSearch && project.status === 'active';
    if (activeTab === 'completed') return matchesSearch && project.status === 'completed';
    if (activeTab === 'archived') return matchesSearch && project.status === 'archived';
    return matchesSearch;
  });

  const tabItems = [
    { key: 'all', label: '全部', count: projects.length },
    { key: 'active', label: '进行中', count: stats.active },
    { key: 'completed', label: '已完成', count: stats.completed },
    { key: 'archived', label: '已归档', count: stats.archived },
  ];

  return (
    <div className="space-y-8 p-8">
      {/* 页面标题 */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white font-display">我的项目</h1>
          <p className="text-gray-400 mt-1">管理和追踪您所有的项目</p>
        </div>
        <Link href="/projects/new">
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            className="bg-gradient-to-r from-orange-500 to-orange-600 border-none shadow-lg hover:shadow-orange-500/30 transition-all hover:-translate-y-0.5"
          >
            新建项目
          </Button>
        </Link>
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
          <Select
            defaultValue="recent"
            className="w-36"
            style={{ backgroundColor: 'rgba(255,255,255,0.03)' }}
            options={[
              { value: 'recent', label: '最近更新' },
              { value: 'created', label: '创建时间' },
              { value: 'name', label: '项目名称' },
              { value: 'progress', label: '进度' },
            ]}
          />
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
      <div className="relative">
        <SearchOutlined className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
        <Input
          placeholder="搜索项目..."
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          className="pl-11 bg-white/5 border-white/10 text-white placeholder-gray-500 focus:border-orange-500 focus:shadow-[0_0_0_3px_rgba(249,115,22,0.2)]"
          allowClear
        />
      </div>

      {/* 项目列表 */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" description="加载中..." />
        </div>
      ) : filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {filteredProjects.map((project, index) => (
            <div
              key={project.id}
              className="animate-fade-in-up"
              style={{ animationDelay: `${index * 0.05}s` }}
            >
              <ProjectCard project={project} index={index} />
            </div>
          ))}
        </div>
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
              <Link href="/projects/new">
                <Button type="primary" icon={<PlusOutlined />} className="bg-orange-500 border-none">
                  创建第一个项目
                </Button>
              </Link>
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
