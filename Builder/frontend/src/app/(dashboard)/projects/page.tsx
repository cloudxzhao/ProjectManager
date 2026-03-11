'use client';

import { useState } from 'react';
import { Card, Input, Select, Tabs, TabsProps, Button, Avatar, Tag, Progress, Empty } from 'antd';
import { PlusOutlined, SearchOutlined, ProjectOutlined } from '@ant-design/icons';
import Link from 'next/link';

const { Option } = Select;

// Mock 数据
const projectsData = [
  {
    id: '1',
    name: '电商平台重构',
    description: '重构现有电商平台，提升用户体验和系统性能',
    progress: 65,
    members: 5,
    status: 'active',
    color: '#f97316',
    icon: '🛒',
  },
  {
    id: '2',
    name: '移动端 APP 开发',
    description: '开发 iOS 和 Android 双平台移动应用',
    progress: 40,
    members: 3,
    status: 'active',
    color: '#8b5cf6',
    icon: '📱',
  },
  {
    id: '3',
    name: '数据分析平台',
    description: '构建数据分析和可视化平台',
    progress: 80,
    members: 4,
    status: 'active',
    color: '#06b6d4',
    icon: '📊',
  },
  {
    id: '4',
    name: 'CRM 系统升级',
    description: '升级客户关系管理系统',
    progress: 25,
    members: 2,
    status: 'active',
    color: '#ec4899',
    icon: '🤝',
  },
  {
    id: '5',
    name: '官网改版',
    description: '公司官网视觉和内容重构',
    progress: 100,
    members: 3,
    status: 'completed',
    color: '#22c55e',
    icon: '🌐',
  },
  {
    id: '6',
    name: '内部工具集',
    description: '开发内部效率工具集合',
    progress: 100,
    members: 2,
    status: 'completed',
    color: '#6366f1',
    icon: '🔧',
  },
];

const statusColorMap: Record<string, string> = {
  active: 'processing',
  completed: 'success',
  archived: 'default',
};

const statusTextMap: Record<string, string> = {
  active: '进行中',
  completed: '已完成',
  archived: '已归档',
};

interface ProjectCardProps {
  project: typeof projectsData[0];
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project }) => {
  return (
    <Link href={`/projects/${project.id}`}>
      <Card
        hoverable
        className="bg-gray-800/50 border-gray-700 hover:border-orange-500/30 transition-all h-full"
      >
        <div className="mb-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div
                className="w-12 h-12 rounded-xl flex items-center justify-center text-2xl"
                style={{ backgroundColor: `${project.color}20` }}
              >
                {project.icon}
              </div>
              <div>
                <h3 className="text-lg font-semibold text-white">{project.name}</h3>
                <Tag
                  color={statusColorMap[project.status] as any}
                  className="mt-1"
                >
                  {statusTextMap[project.status]}
                </Tag>
              </div>
            </div>
          </div>
        </div>

        <p className="text-gray-400 text-sm mb-4 line-clamp-2 h-10">
          {project.description}
        </p>

        <div className="mb-4">
          <Progress
            percent={project.progress}
            strokeColor={{ from: project.color, to: `${project.color}aa` }}
            trailColor="rgba(255,255,255,0.1)"
            format={(percent) => (
              <span className="text-white text-xs">{percent}%</span>
            )}
          />
        </div>

        <div className="flex items-center justify-between">
          <div className="flex -space-x-2">
            {Array.from({ length: Math.min(project.members, 4) }).map((_, i) => (
              <Avatar
                key={i}
                size={28}
                className="bg-gray-600 border-2 border-gray-800"
                icon={<span className="text-xs">U{i + 1}</span>}
              />
            ))}
            {project.members > 4 && (
              <Avatar
                size={28}
                className="bg-gray-600 border-2 border-gray-800 text-xs"
                icon={<span>+{project.members - 4}</span>}
              />
            )}
          </div>
          <span className="text-gray-500 text-xs">
            {project.members} 名成员
          </span>
        </div>
      </Card>
    </Link>
  );
};

export default function ProjectsPage() {
  const [activeTab, setActiveTab] = useState('all');
  const [searchValue, setSearchValue] = useState('');

  const filteredProjects = projectsData.filter((project) => {
    const matchesSearch = project.name.toLowerCase().includes(searchValue.toLowerCase()) ||
      project.description.toLowerCase().includes(searchValue.toLowerCase());

    if (activeTab === 'all') return matchesSearch;
    if (activeTab === 'active') return matchesSearch && project.status === 'active';
    if (activeTab === 'completed') return matchesSearch && project.status === 'completed';
    if (activeTab === 'archived') return matchesSearch && project.status === 'archived';
    return matchesSearch;
  });

  const tabItems: TabsProps['items'] = [
    { key: 'all', label: '全部' },
    { key: 'active', label: '进行中' },
    { key: 'completed', label: '已完成' },
    { key: 'archived', label: '已归档' },
  ];

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">项目列表</h1>
          <p className="text-gray-400 mt-1">管理和查看所有项目</p>
        </div>
        <Link href="/projects/new">
          <Button
            type="primary"
            icon={<PlusOutlined />}
            className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
          >
            新建项目
          </Button>
        </Link>
      </div>

      {/* 搜索和筛选 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <div className="flex items-center gap-4 flex-wrap">
          <Input
            placeholder="搜索项目..."
            prefix={<SearchOutlined className="text-gray-400" />}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            className="w-full sm:w-80 bg-gray-700/50 border-gray-600"
            allowClear
          />
          <Select
            placeholder="项目状态"
            className="w-full sm:w-40"
            defaultValue="all"
            options={[
              { value: 'all', label: '全部状态' },
              { value: 'active', label: '进行中' },
              { value: 'completed', label: '已完成' },
              { value: 'archived', label: '已归档' },
            ]}
          />
        </div>
      </Card>

      {/* 项目列表 */}
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
        className="project-tabs"
        tabBarStyle={{ borderBottomColor: '#374151' }}
      />

      {filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      ) : (
        <Card className="bg-gray-800/50 border-gray-700">
          <Empty
            description="暂无项目"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
          <div className="text-center mt-4">
            <Link href="/projects/new">
              <Button type="primary" icon={<PlusOutlined />}>
                创建第一个项目
              </Button>
            </Link>
          </div>
        </Card>
      )}
    </div>
  );
}
