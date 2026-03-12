'use client';

import { Card, Row, Col, Statistic, Progress, Avatar, List, Tag, Button } from 'antd';
import {
  ProjectOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  FireOutlined,
  PlusOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import Link from 'next/link';

// Mock 数据
const statsData = [
  { title: '进行中项目', value: 5, icon: <ProjectOutlined />, trend: 'up', trendValue: 2 },
  { title: '待处理任务', value: 12, icon: <ClockCircleOutlined />, trend: 'down', trendValue: 3 },
  { title: '已完成任务', value: 48, icon: <CheckCircleOutlined />, trend: 'up', trendValue: 8 },
  { title: '本周速度', value: 24, icon: <FireOutlined />, suffix: '点', trend: 'up', trendValue: 5 },
];

const ongoingProjects = [
  { id: '1', name: '电商平台重构', progress: 65, members: 5, status: 'active' },
  { id: '2', name: '移动端 APP 开发', progress: 40, members: 3, status: 'active' },
  { id: '3', name: '数据分析平台', progress: 80, members: 4, status: 'active' },
  { id: '4', name: 'CRM 系统升级', progress: 25, members: 2, status: 'active' },
];

const pendingTasks = [
  { id: '1', title: '完成用户登录模块', priority: 'high', dueDate: '今天', assignee: '张三' },
  { id: '2', title: '优化数据库查询性能', priority: 'medium', dueDate: '明天', assignee: '李四' },
  { id: '3', title: '编写 API 文档', priority: 'low', dueDate: '本周五', assignee: '王五' },
  { id: '4', title: '代码审查 - 支付模块', priority: 'high', dueDate: '今天', assignee: '赵六' },
];

const recentActivities = [
  { id: '1', user: '张三', action: '完成了任务', target: '用户登录界面设计', time: '10 分钟前' },
  { id: '2', user: '李四', action: '创建了任务', target: '修复支付 bug', time: '30 分钟前' },
  { id: '3', user: '王五', action: '评论了', target: 'API 接口设计文档', time: '1 小时前' },
  { id: '4', user: '赵六', action: '添加了新成员', target: '电商平台重构项目', time: '2 小时前' },
];

const priorityColors: Record<string, string> = {
  high: 'red',
  medium: 'orange',
  low: 'green',
};

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-white">工作台</h1>
          <p className="text-gray-400 text-sm mt-1">欢迎回来，今天也是高效的一天！</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none h-10 px-5 rounded-xl glow-orange hover:-translate-y-0.5 transition-all"
        >
          快速创建
        </Button>
      </div>

      {/* 核心指标卡片 */}
      <Row gutter={[16, 16]}>
        {statsData.map((stat, index) => (
          <Col xs={24} sm={12} lg={6} key={index}>
            <Card className="glass-card bg-white/3 border-white/8 hover:bg-white/5 hover:border-orange-500/30 transition-all duration-300">
              <Statistic
                title={<span className="text-gray-400 text-sm">{stat.title}</span>}
                value={stat.value}
                suffix={stat.suffix}
                prefix={
                  <span className="text-orange-400 text-2xl mr-2">{stat.icon}</span>
                }
                valueStyle={{ color: '#fff', fontFamily: 'Outfit, sans-serif', fontWeight: 600 }}
              />
              <div className="mt-2 flex items-center gap-2">
                {stat.trend === 'up' ? (
                  <ArrowUpOutlined className="text-emerald-400" />
                ) : (
                  <ArrowDownOutlined className="text-red-400" />
                )}
                <span className={stat.trend === 'up' ? 'text-emerald-400' : 'text-red-400'}>
                  {stat.trendValue > 0 ? '+' : ''}{stat.trendValue}
                </span>
                <span className="text-gray-500 text-xs">较上周</span>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      {/* Bento Grid 布局 */}
      <Row gutter={[16, 16]}>
        {/* 进行中项目 */}
        <Col xs={24} lg={16}>
          <Card
            title={<span className="text-white font-display font-semibold">进行中项目</span>}
            extra={
              <Link href="/projects" className="text-orange-400 hover:text-orange-300 text-sm transition-colors">
                查看全部 →
              </Link>
            }
            className="glass-card bg-white/3 border-white/8"
          >
            <List
              grid={{ gutter: 16, column: 2 }}
              dataSource={ongoingProjects}
              renderItem={(project) => (
                <List.Item>
                  <Card
                    className="bg-white/3 border-white/8 hover:bg-white/5 hover:border-orange-500/30 transition-all duration-300 cursor-pointer"
                    bodyStyle={{ padding: '16px' }}
                  >
                    <div className="mb-3">
                      <h3 className="text-white font-medium truncate">{project.name}</h3>
                    </div>
                    <Progress
                      percent={project.progress}
                      strokeColor={{ from: '#f97316', to: '#eab308' }}
                      trailColor="rgba(255,255,255,0.1)"
                      format={(percent) => <span className="text-white text-xs">{percent}%</span>}
                    />
                    <div className="mt-3 flex items-center justify-between">
                      <div className="flex -space-x-2">
                        {Array.from({ length: Math.min(project.members, 3) }).map((_, i) => (
                          <Avatar
                            key={i}
                            size={24}
                            className="bg-gradient-to-br from-purple-400 to-pink-500 border-2 border-gray-700"
                            icon={<span className="text-xs">U{i + 1}</span>}
                          />
                        ))}
                        {project.members > 3 && (
                          <Avatar
                            size={24}
                            className="bg-gray-600 border-2 border-gray-700"
                            icon={<span className="text-xs">+{project.members - 3}</span>}
                          />
                        )}
                      </div>
                      <Tag color="orange" className="border-orange-500/50 bg-orange-500/10 text-orange-400">
                        进行中
                      </Tag>
                    </div>
                  </Card>
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 待处理任务 */}
        <Col xs={24} lg={8}>
          <Card
            title={<span className="text-white font-display font-semibold">待处理任务</span>}
            extra={
              <Link href="/tasks" className="text-orange-400 hover:text-orange-300 text-sm transition-colors">
                查看全部 →
              </Link>
            }
            className="glass-card bg-white/3 border-white/8"
          >
            <List
              dataSource={pendingTasks}
              renderItem={(task) => (
                <List.Item className="border-gray-700/50 hover:bg-white/5 transition-all cursor-pointer px-0">
                  <div className="flex-1 py-2">
                    <div className="flex items-start justify-between mb-2">
                      <h4 className="text-white text-sm font-medium line-clamp-1">{task.title}</h4>
                      <Tag
                        color={task.priority === 'high' ? 'red' : task.priority === 'medium' ? 'orange' : 'green'}
                        className={`${
                          task.priority === 'high' ? 'bg-red-500/10 text-red-400 border-red-500/30' :
                          task.priority === 'medium' ? 'bg-orange-500/10 text-orange-400 border-orange-500/30' :
                          'bg-green-500/10 text-green-400 border-green-500/30'
                        } text-xs`}
                      >
                        {task.priority === 'high' ? '高' : task.priority === 'medium' ? '中' : '低'}
                      </Tag>
                    </div>
                    <div className="flex items-center gap-4 text-xs text-gray-400">
                      <span>{task.assignee}</span>
                      <span>截止：{task.dueDate}</span>
                    </div>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 最近活动 */}
        <Col xs={24}>
          <Card
            title={<span className="text-white font-display font-semibold">最近活动</span>}
            className="glass-card bg-white/3 border-white/8"
          >
            <List
              dataSource={recentActivities}
              renderItem={(activity) => (
                <List.Item className="border-gray-700/50 py-3">
                  <div className="flex items-center gap-3 w-full">
                    <Avatar className="bg-gradient-to-br from-orange-400 to-amber-500">
                      {activity.user[0]}
                    </Avatar>
                    <div className="flex-1">
                      <p className="text-gray-300 text-sm">
                        <span className="text-white font-medium">{activity.user}</span>{' '}
                        <span className="text-gray-400">{activity.action}</span>{' '}
                        <span className="text-orange-400 hover:text-orange-300 cursor-pointer">{activity.target}</span>
                      </p>
                      <p className="text-gray-500 text-xs mt-1">{activity.time}</p>
                    </div>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
