'use client';

import { useState, useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card, Table, Tag, Input, Select, Button, Space, Typography, Empty, Spin, Pagination,
  Badge, Tooltip, DatePicker,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  FilterOutlined,
  ClockCircleOutlined,
  FlagOutlined,
  CheckCircleOutlined,
  ProjectOutlined,
  UserOutlined,
  CalendarOutlined,
} from '@ant-design/icons';
import { getProjects } from '@/lib/api/project';
import { getTasks as getTasksApi } from '@/lib/api/task';
import type { Task, TaskStatus, Priority } from '@/lib/api/task';
import type { Project } from '@/lib/api/project';
import dayjs from 'dayjs';

const { Text } = Typography;
const { Option } = Select;

// 任务状态映射
const statusConfig: Record<TaskStatus, { label: string; color: string }> = {
  todo: { label: '待办', color: 'gray' },
  in_progress: { label: '进行中', color: 'blue' },
  testing: { label: '测试中', color: 'orange' },
  done: { label: '已完成', color: 'green' },
};

// 优先级映射
const priorityConfig: Record<Priority, { label: string; color: string }> = {
  low: { label: '低', color: 'green' },
  medium: { label: '中', color: 'orange' },
  high: { label: '高', color: 'red' },
  urgent: { label: '紧急', color: 'purple' },
};

// 表格数据类型
interface TaskTableItem {
  key: string;
  id: number;
  projectId: number;
  projectName?: string;
  title: string;
  status: TaskStatus;
  priority: Priority;
  assignee?: string;
  dueDate?: string;
  storyPoints?: number;
  createdAt: string;
}

export default function TasksPage() {
  const router = useRouter();

  // 状态
  const [loading, setLoading] = useState(false);
  const [projects, setProjects] = useState<Project[]>([]);
  const [allTasks, setAllTasks] = useState<TaskTableItem[]>([]);
  const [filteredTasks, setFilteredTasks] = useState<TaskTableItem[]>([]);
  const [total, setTotal] = useState(0);

  // 筛选条件
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedProject, setSelectedProject] = useState<string>('all');
  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [selectedPriority, setSelectedPriority] = useState<string>('all');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
  });

  // 加载项目和任务
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        // 获取项目列表
        const projectsData = await getProjects(1, 100);
        setProjects(projectsData.list || []);

        // 获取所有项目的任务
        const allTasksData: TaskTableItem[] = [];
        for (const project of projectsData.list || []) {
          try {
            const tasksData = await getTasksApi(project.id);
            const tasksWithProjectName = (tasksData.list || []).map((task: Task) => ({
              key: `task-${task.id}`,
              id: task.id,
              projectId: project.id,
              projectName: project.name,
              title: task.title,
              status: task.status,
              priority: task.priority,
              assignee: task.assigneeId?.toString(),
              dueDate: task.dueDate,
              storyPoints: task.storyPoints,
              createdAt: task.createdAt,
            }));
            allTasksData.push(...tasksWithProjectName);
          } catch (error) {
            console.error(`获取项目 ${project.name} 的任务失败:`, error);
          }
        }
        setAllTasks(allTasksData);
        setTotal(allTasksData.length);
      } catch (error) {
        console.error('加载数据失败:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // 筛选任务
  useEffect(() => {
    let result = [...allTasks];

    // 关键词搜索
    if (searchKeyword) {
      result = result.filter((task) =>
        task.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        task.projectName?.toLowerCase().includes(searchKeyword.toLowerCase())
      );
    }

    // 项目筛选
    if (selectedProject !== 'all') {
      result = result.filter((task) => task.projectId === Number(selectedProject));
    }

    // 状态筛选
    if (selectedStatus !== 'all') {
      result = result.filter((task) => task.status === selectedStatus);
    }

    // 优先级筛选
    if (selectedPriority !== 'all') {
      result = result.filter((task) => task.priority === selectedPriority);
    }

    setFilteredTasks(result);
    setTotal(result.length);

    // 重置分页到第一页
    if (pagination.current !== 1) {
      setPagination((prev) => ({ ...prev, current: 1 }));
    }
  }, [searchKeyword, selectedProject, selectedStatus, selectedPriority, allTasks]);

  // 分页数据
  const paginatedTasks = useMemo(() => {
    const start = (pagination.current - 1) * pagination.pageSize;
    const end = start + pagination.pageSize;
    return filteredTasks.slice(start, end);
  }, [filteredTasks, pagination]);

  // 处理分页变化
  const handleTableChange = (newPagination: any) => {
    setPagination({
      current: newPagination.current || 1,
      pageSize: newPagination.pageSize || 20,
    });
  };

  // 表格列定义
  const columns = [
    {
      title: '任务标题',
      dataIndex: 'title',
      key: 'title',
      fixed: 'left' as const,
      width: 280,
      render: (text: string, record: TaskTableItem) => (
        <div className="flex flex-col gap-1">
          <Text
            className="cursor-pointer hover:text-orange-500 transition-colors font-medium"
            onClick={() => router.push(`/tasks/${record.id}`)}
          >
            {text}
          </Text>
          <div className="flex items-center gap-2">
            <Tag color="blue" className="text-xs">
              <ProjectOutlined className="mr-1" />
              {record.projectName}
            </Tag>
            <Text type="secondary" className="text-xs">
              TASK-{record.id}
            </Text>
          </div>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TaskStatus) => {
        const config = statusConfig[status];
        return (
          <Tag color={config.color} className="text-xs">
            <CheckCircleOutlined className="mr-1" />
            {config.label}
          </Tag>
        );
      },
      filters: Object.entries(statusConfig).map(([value, config]) => ({
        text: config.label,
        value,
      })),
      onFilter: (value: any, record: TaskTableItem) => record.status === value,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 90,
      render: (priority: Priority) => {
        const config = priorityConfig[priority];
        return (
          <Tag color={config.color} className="text-xs">
            <FlagOutlined className="mr-1" />
            {config.label}
          </Tag>
        );
      },
      filters: Object.entries(priorityConfig).map(([value, config]) => ({
        text: config.label,
        value,
      })),
      onFilter: (value: any, record: TaskTableItem) => record.priority === value,
    },
    {
      title: '负责人',
      dataIndex: 'assignee',
      key: 'assignee',
      width: 120,
      render: (assignee?: string) => (
        <div className="flex items-center gap-2">
          <UserOutlined className="text-gray-400" />
          <Text type="secondary" className="text-sm">
            {assignee || '未分配'}
          </Text>
        </div>
      ),
    },
    {
      title: '截止日期',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 130,
      render: (dueDate?: string) => (
        <div className="flex items-center gap-2">
          <ClockCircleOutlined className={dueDate && dayjs(dueDate).isBefore(dayjs()) ? 'text-red-500' : 'text-gray-400'} />
          <Text type="secondary" className={`text-sm ${dueDate && dayjs(dueDate).isBefore(dayjs()) ? 'text-red-500' : ''}`}>
            {dueDate ? dayjs(dueDate).format('YYYY-MM-DD') : '-'}
          </Text>
        </div>
      ),
      sorter: (a: TaskTableItem, b: TaskTableItem) => {
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return dayjs(a.dueDate).valueOf() - dayjs(b.dueDate).valueOf();
      },
    },
    {
      title: '故事点',
      dataIndex: 'storyPoints',
      key: 'storyPoints',
      width: 80,
      align: 'center' as const,
      render: (storyPoints?: number) => (
        <span className="text-sm text-gray-600">{storyPoints || '-'}</span>
      ),
      sorter: (a: TaskTableItem, b: TaskTableItem) => (a.storyPoints || 0) - (b.storyPoints || 0),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (createdAt: string) => (
        <div className="flex items-center gap-2">
          <CalendarOutlined className="text-gray-400" />
          <Text type="secondary" className="text-sm">
            {dayjs(createdAt).format('MM-DD')}
          </Text>
        </div>
      ),
      sorter: (a: TaskTableItem, b: TaskTableItem) =>
        dayjs(a.createdAt).valueOf() - dayjs(b.createdAt).valueOf(),
    },
  ];

  // 清空筛选
  const handleClearFilters = () => {
    setSearchKeyword('');
    setSelectedProject('all');
    setSelectedStatus('all');
    setSelectedPriority('all');
  };

  return (
    <div className="space-y-4">
      {/* 页面头部 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">任务中心</h1>
          <p className="text-gray-400 text-sm mt-1">查看所有项目的任务</p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} className="bg-orange-500 hover:bg-orange-600">
          创建任务
        </Button>
      </div>

      {/* 筛选区域 */}
      <Card className="glass-dark border-white/8">
        <Space wrap size="middle" className="w-full">
          <Input
            placeholder="搜索任务标题..."
            prefix={<SearchOutlined className="text-gray-400" />}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            allowClear
            className="w-64 bg-white/5 border-white/10 text-white placeholder-gray-500"
            style={{ backgroundColor: 'rgba(255,255,255,0.05)' }}
          />

          <Select
            value={selectedProject}
            onChange={setSelectedProject}
            className="w-40"
            placeholder="选择项目"
            allowClear
          >
            <Option value="all">全部项目</Option>
            {projects.map((project) => (
              <Option key={project.id} value={project.id}>
                {project.name}
              </Option>
            ))}
          </Select>

          <Select
            value={selectedStatus}
            onChange={setSelectedStatus}
            className="w-32"
            placeholder="选择状态"
            allowClear
          >
            <Option value="all">全部状态</Option>
            {Object.entries(statusConfig).map(([value, config]) => (
              <Option key={value} value={value}>
                {config.label}
              </Option>
            ))}
          </Select>

          <Select
            value={selectedPriority}
            onChange={setSelectedPriority}
            className="w-32"
            placeholder="选择优先级"
            allowClear
          >
            <Option value="all">全部优先级</Option>
            {Object.entries(priorityConfig).map(([value, config]) => (
              <Option key={value} value={value}>
                {config.label}
              </Option>
            ))}
          </Select>

          <Button icon={<FilterOutlined />} onClick={handleClearFilters}>
            清空筛选
          </Button>
        </Space>
      </Card>

      {/* 任务表格 */}
      <Card className="glass-dark border-white/8">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Badge count={total} overflowCount={999} showZero className="[&_.ant-badge-count]:bg-orange-500" />
            <Text className="text-gray-400">共 {total} 个任务</Text>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-16">
            <Spin size="large" tip="加载中..." />
          </div>
        ) : filteredTasks.length === 0 ? (
          <Empty
            description={
              searchKeyword || selectedProject !== 'all' || selectedStatus !== 'all' || selectedPriority !== 'all'
                ? '暂无符合条件的任务'
                : '暂无任务'
            }
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        ) : (
          <>
            <Table
              columns={columns}
              dataSource={paginatedTasks}
              pagination={false}
              scroll={{ x: 1000 }}
              rowClassName="hover:bg-white/5 transition-colors cursor-pointer"
              onRow={(record) => ({
                onClick: () => router.push(`/tasks/${record.id}`),
              })}
            />
            <div className="flex justify-end mt-4">
              <Pagination
                current={pagination.current}
                pageSize={pagination.pageSize}
                total={filteredTasks.length}
                onChange={(page, pageSize) =>
                  setPagination({ current: page, pageSize: pageSize || 20 })
                }
                showSizeChanger
                showTotal={(total) => `共 ${total} 条`}
              />
            </div>
          </>
        )}
      </Card>
    </div>
  );
}
