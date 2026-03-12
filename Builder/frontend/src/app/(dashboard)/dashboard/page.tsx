'use client';

import { Avatar } from 'antd';
import type { ReactNode } from 'react';

// 图标组件
const ProjectIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
  </svg>
);

const TaskIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 20h9"></path>
    <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
  </svg>
);

const CheckIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

const FireIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.1.2-2.2.5-3.3a7 7 0 0 0 3 3.3z"></path>
  </svg>
);

const GlobeIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"></circle>
    <line x1="2" y1="12" x2="22" y2="12"></line>
    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
  </svg>
);

const MobileIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
    <line x1="12" y1="18" x2="12.01" y2="18"></line>
  </svg>
);

const LayersIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
    <path d="M2 17l10 5 10-5"></path>
    <path d="M2 12l10 5 10-5"></path>
  </svg>
);

const MarketingIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline>
    <polyline points="17 6 23 6 23 12"></polyline>
  </svg>
);

const TeamIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
    <circle cx="9" cy="7" r="4"></circle>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
  </svg>
);

const ChartIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="20" x2="18" y2="10"></line>
    <line x1="12" y1="20" x2="12" y2="4"></line>
    <line x1="6" y1="20" x2="6" y2="14"></line>
  </svg>
);

const ClockIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"></circle>
    <polyline points="12 6 12 12 16 14"></polyline>
  </svg>
);

const ArrowUpIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="18 15 12 9 6 15"></polyline>
  </svg>
);

const ArrowDownIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="6 9 12 15 18 9"></polyline>
  </svg>
);

const CheckSquareIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

// Mock 数据
const metricsData = [
  {
    title: '进行中项目',
    value: 12,
    icon: <ProjectIcon />,
    trend: 'up' as const,
    trendValue: 12,
    color: '#f97316',
    colorDark: '#ea580c',
    glow: 'rgba(249, 115, 22, 0.4)',
  },
  {
    title: '待完成任务',
    value: 48,
    icon: <TaskIcon />,
    trend: 'up' as const,
    trendValue: 8,
    color: '#ec4899',
    colorDark: '#db2777',
    glow: 'rgba(236, 72, 153, 0.4)',
  },
  {
    title: '本周已完成',
    value: 156,
    icon: <CheckIcon />,
    trend: 'up' as const,
    trendValue: 24,
    color: '#10b981',
    colorDark: '#059669',
    glow: 'rgba(16, 185, 129, 0.4)',
  },
  {
    title: '逾期任务',
    value: 8,
    icon: <ClockIcon />,
    trend: 'down' as const,
    trendValue: 3,
    color: '#8b5cf6',
    colorDark: '#7c3aed',
    glow: 'rgba(139, 92, 246, 0.4)',
  },
];

const projectsData = [
  {
    id: '1',
    name: '全球电商平台重构',
    members: 12,
    daysLeft: 24,
    progress: 68,
    status: 'active',
    statusText: '进行中',
    color: '#f97316',
    colorDark: '#ea580c',
    icon: 'globe',
  },
  {
    id: '2',
    name: '移动端 App 2.0',
    members: 8,
    daysLeft: 45,
    progress: 42,
    status: 'active',
    statusText: '进行中',
    color: '#8b5cf6',
    colorDark: '#7c3aed',
    icon: 'mobile',
  },
  {
    id: '3',
    name: '数据中台建设',
    members: 15,
    daysLeft: 60,
    progress: 28,
    status: 'delayed',
    statusText: '延期',
    color: '#06b6d4',
    colorDark: '#0891b2',
    icon: 'layers',
  },
  {
    id: '4',
    name: '营销自动化系统',
    members: 6,
    daysLeft: 12,
    progress: 85,
    status: 'review',
    statusText: '评审中',
    color: '#84cc16',
    colorDark: '#65a30d',
    icon: 'marketing',
  },
];

const tasksData = [
  {
    id: '1',
    title: '完成用户模块 API 设计',
    priority: 'high' as const,
    priorityText: '高优先级',
    dueDate: '今天截止',
    checked: false,
  },
  {
    id: '2',
    title: 'Review 前端 PR #245',
    priority: 'medium' as const,
    priorityText: '中优先级',
    dueDate: '明天截止',
    checked: false,
  },
  {
    id: '3',
    title: '周会准备 Sprint 回顾材料',
    priority: 'medium' as const,
    priorityText: '中优先级',
    dueDate: '本周五',
    checked: false,
  },
  {
    id: '4',
    title: '更新项目文档',
    priority: 'low' as const,
    priorityText: '低优先级',
    dueDate: '下周截止',
    checked: false,
  },
  {
    id: '5',
    title: '数据库性能优化方案',
    priority: 'high' as const,
    priorityText: '高优先级',
    dueDate: '3 天后截止',
    checked: false,
  },
];

const activitiesData = [
  {
    id: '1',
    user: '李明',
    userChar: '李',
    action: '完成了任务',
    target: '"登录页面优化"',
    time: '2 分钟前',
    color: '#f97316',
    colorDark: '#ea580c',
  },
  {
    id: '2',
    user: '王芳',
    userChar: '王',
    action: '提交了代码到',
    target: '移动端 App',
    time: '15 分钟前',
    color: '#8b5cf6',
    colorDark: '#7c3aed',
  },
  {
    id: '3',
    user: '陈伟',
    userChar: '陈',
    action: '创建了新的',
    target: 'Wiki 文档',
    time: '1 小时前',
    color: '#06b6d4',
    colorDark: '#0891b2',
  },
  {
    id: '4',
    user: '刘强',
    userChar: '刘',
    action: '评论了',
    target: 'Issue #89',
    time: '2 小时前',
    color: '#10b981',
    colorDark: '#059669',
  },
];

const chartData = [
  { day: '一', value: 45 },
  { day: '二', value: 72 },
  { day: '三', value: 58 },
  { day: '四', value: 85 },
  { day: '五', value: 62 },
  { day: '六', value: 35 },
  { day: '日', value: 28 },
];

// 图标映射
const projectIcons: Record<string, ReactNode> = {
  globe: <GlobeIcon />,
  mobile: <MobileIcon />,
  layers: <LayersIcon />,
  marketing: <MarketingIcon />,
};

// 任务复选框组件
const TaskCheckbox = ({ checked, onChange }: { checked: boolean; onChange: () => void }) => (
  <div
    className={`task-checkbox ${checked ? 'checked' : ''}`}
    onClick={onChange}
  >
    <CheckSquareIcon />
  </div>
);

export default function DashboardPage() {
  return (
    <div className="dashboard">
      {/* 背景图层 */}
      <div className="bg-layer">
        <div className="bg-overlay"></div>
        <div className="grid-pattern"></div>
      </div>

      {/* 页面标题 */}
      <div className="dashboard-header">
        <h1 className="dashboard-title">欢迎回来，张三丰</h1>
        <p className="dashboard-subtitle">这里是您所有项目和任务的总览面板</p>
      </div>

      {/* 关键指标卡片 */}
      <div className="metrics-grid">
        {metricsData.map((metric, index) => (
          <div
            key={index}
            className="metric-card animate-fade-in"
            style={{
              '--metric-color': metric.color,
              '--metric-color-dark': metric.colorDark,
              '--metric-glow': metric.glow,
              animationDelay: `${(index + 1) * 0.1}s`,
            } as React.CSSProperties}
          >
            <div className="metric-header">
              <div className="metric-icon">
                {metric.icon}
              </div>
              <div className={`metric-change ${metric.trend === 'up' ? 'positive' : 'negative'}`}>
                {metric.trend === 'up' ? <ArrowUpIcon /> : <ArrowDownIcon />}
                {metric.trend === 'up' ? '+' : '-'}{metric.trendValue}%
              </div>
            </div>
            <div className="metric-value">{metric.value}</div>
            <div className="metric-label">{metric.title}</div>
          </div>
        ))}
      </div>

      {/* Bento Grid 布局 */}
      <div className="bento-grid">
        {/* 项目概览 */}
        <div className="bento-card large">
          <div className="card-header">
            <h3 className="card-title">
              <ProjectIcon />
              项目概览
            </h3>
            <a href="/projects" className="card-action">查看全部 →</a>
          </div>
          <div className="project-list">
            {projectsData.map((project) => (
              <div
                key={project.id}
                className="project-item"
                style={{
                  '--project-color': project.color,
                  '--project-color-dark': project.colorDark,
                } as React.CSSProperties}
              >
                <div className="project-icon">
                  {projectIcons[project.icon]}
                </div>
                <div className="project-info">
                  <div className="project-name">{project.name}</div>
                  <div className="project-meta">
                    <span>{project.members} 成员</span>
                    <span>剩余 {project.daysLeft} 天</span>
                  </div>
                </div>
                <div className="project-progress">
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{ width: `${project.progress}%` }}
                    />
                  </div>
                </div>
                <span className={`project-status status-${project.status}`}>
                  {project.statusText}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* 待办任务 */}
        <div className="bento-card large">
          <div className="card-header">
            <h3 className="card-title">
              <TaskIcon />
              待办任务
            </h3>
            <a href="/tasks" className="card-action">查看全部 →</a>
          </div>
          <div className="task-list">
            {tasksData.map((task) => (
              <TaskItem key={task.id} task={task} />
            ))}
          </div>
        </div>

        {/* 团队动态 */}
        <div className="bento-card">
          <div className="card-header">
            <h3 className="card-title">
              <TeamIcon />
              团队动态
            </h3>
          </div>
          <div className="activity-list">
            {activitiesData.map((activity) => (
              <div
                key={activity.id}
                className="activity-item"
                style={{
                  '--activity-color': activity.color,
                  '--activity-color-dark': activity.colorDark,
                } as React.CSSProperties}
              >
                <Avatar
                  className="activity-avatar"
                  style={{
                    background: `linear-gradient(135deg, ${activity.color} 0%, ${activity.colorDark} 100%)`,
                  }}
                >
                  {activity.userChar}
                </Avatar>
                <div className="activity-content">
                  <div className="activity-text">
                    <strong>{activity.user}</strong> {activity.action} <span className="text-accent">{activity.target}</span>
                  </div>
                  <div className="activity-time">{activity.time}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 本周趋势 */}
        <div className="bento-card">
          <div className="card-header">
            <h3 className="card-title">
              <ChartIcon />
              本周趋势
            </h3>
          </div>
          <div className="chart-container">
            {chartData.map((item, index) => (
              <div
                key={index}
                className="chart-bar"
                style={{ height: `${item.value}%` }}
                data-label={item.day}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// 任务项组件（需要内部状态管理复选框）
function TaskItem({ task }: { task: typeof tasksData[0] }) {
  const isChecked = false; // 可以在这里添加状态管理

  const priorityColors = {
    high: 'priority-high',
    medium: 'priority-medium',
    low: 'priority-low',
  };

  return (
    <div className="task-item">
      <TaskCheckbox checked={isChecked} onChange={() => {}} />
      <div className="task-content">
        <div className="task-title">{task.title}</div>
        <div className="task-meta">
          <span className={`task-priority ${priorityColors[task.priority]}`}>
            {task.priorityText}
          </span>
          <span>{task.dueDate}</span>
        </div>
      </div>
    </div>
  );
}
