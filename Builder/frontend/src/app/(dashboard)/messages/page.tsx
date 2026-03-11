'use client';

import { useState } from 'react';
import { Card, List, Button, Tag, Avatar, Input, Space, Popconfirm, message as antdMessage, Empty as AntEmpty, Dropdown, MenuProps } from 'antd';
import { Empty as AppEmpty } from '@/components/common';
import {
  BellOutlined,
  SearchOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
  ProjectOutlined,
  MessageOutlined,
  UserOutlined,
  SettingOutlined,
  ClockCircleOutlined,
  StarOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

// Mock 通知数据
const mockNotifications = [
  {
    id: '1',
    type: 'task',
    title: '新任务分配',
    content: '李四将任务「完成用户登录模块」分配给你',
    projectId: '1',
    projectName: '电商平台重构',
    isRead: false,
    isStarred: true,
    createdAt: dayjs().subtract(10, 'minute'),
  },
  {
    id: '2',
    type: 'comment',
    title: '新评论',
    content: '张三评论了任务「设计数据库架构」',
    projectId: '1',
    projectName: '电商平台重构',
    isRead: false,
    isStarred: false,
    createdAt: dayjs().subtract(30, 'minute'),
  },
  {
    id: '3',
    type: 'project',
    title: '项目更新',
    content: '「移动端 APP 开发」项目状态已更新为进行中',
    projectId: '2',
    projectName: '移动端 APP 开发',
    isRead: false,
    isStarred: false,
    createdAt: dayjs().subtract(2, 'hour'),
  },
  {
    id: '4',
    type: 'mention',
    title: '有人@你',
    content: '王五在评论中提到了你',
    projectId: '1',
    projectName: '电商平台重构',
    isRead: true,
    isStarred: false,
    createdAt: dayjs().subtract(1, 'day'),
  },
  {
    id: '5',
    type: 'system',
    title: '系统通知',
    content: '系统将于本周六凌晨进行维护，预计持续 2 小时',
    projectId: null,
    projectName: null,
    isRead: true,
    isStarred: false,
    createdAt: dayjs().subtract(2, 'day'),
  },
  {
    id: '6',
    type: 'task',
    title: '任务已完成',
    content: '赵六完成了任务「API 接口设计」',
    projectId: '3',
    projectName: '数据分析平台',
    isRead: true,
    isStarred: false,
    createdAt: dayjs().subtract(3, 'day'),
  },
];

const typeConfig: Record<string, { icon: React.ReactNode; color: string; bgClass: string }> = {
  task: {
    icon: <CheckCircleOutlined />,
    color: '#10b981',
    bgClass: 'bg-green-500/20',
  },
  comment: {
    icon: <MessageOutlined />,
    color: '#3b82f6',
    bgClass: 'bg-blue-500/20',
  },
  project: {
    icon: <ProjectOutlined />,
    color: '#f97316',
    bgClass: 'bg-orange-500/20',
  },
  mention: {
    icon: <UserOutlined />,
    color: '#8b5cf6',
    bgClass: 'bg-purple-500/20',
  },
  system: {
    icon: <SettingOutlined />,
    color: '#6b7280',
    bgClass: 'bg-gray-500/20',
  },
};

export default function MessagesPage() {
  const [notifications, setNotifications] = useState(mockNotifications);
  const [filter, setFilter] = useState<'all' | 'unread' | 'starred'>('all');
  const [searchValue, setSearchValue] = useState('');

  // 计算未读数
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  // 过滤通知
  const filteredNotifications = notifications.filter((notification) => {
    // 筛选条件
    if (filter === 'unread' && notification.isRead) return false;
    if (filter === 'starred' && !notification.isStarred) return false;

    // 搜索条件
    if (searchValue) {
      const searchLower = searchValue.toLowerCase();
      return (
        notification.title.toLowerCase().includes(searchLower) ||
        notification.content.toLowerCase().includes(searchLower) ||
        notification.projectName?.toLowerCase().includes(searchLower)
      );
    }

    return true;
  });

  // 标记为已读
  const markAsRead = (id: string) => {
    setNotifications(
      notifications.map((n) => (n.id === id ? { ...n, isRead: true } : n))
    );
    antdMessage.success('已标记为已读');
  };

  // 全部标记为已读
  const markAllAsRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, isRead: true })));
    antdMessage.success('全部标记为已读');
  };

  // 切换收藏状态
  const toggleStar = (id: string) => {
    setNotifications(
      notifications.map((n) =>
        n.id === id ? { ...n, isStarred: !n.isStarred } : n
      )
    );
  };

  // 删除通知
  const deleteNotification = (id: string) => {
    setNotifications(notifications.filter((n) => n.id !== id));
    antdMessage.success('通知已删除');
  };

  // 批量操作菜单
  const batchMenuItems: MenuProps['items'] = [
    {
      key: 'markAllRead',
      icon: <CheckCircleOutlined />,
      label: '全部标记为已读',
      onClick: markAllAsRead,
    },
  ];

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <BellOutlined className="text-orange-400" />
            消息中心
          </h1>
          <p className="text-gray-400 mt-1">
            查看你的通知和消息
            {unreadCount > 0 && (
              <Tag color="red" className="ml-2">
                {unreadCount} 条未读
              </Tag>
            )}
          </p>
        </div>
        <Dropdown menu={{ items: batchMenuItems }} trigger={['click']}>
          <Button className="border-gray-600 text-gray-300">
            批量操作
          </Button>
        </Dropdown>
      </div>

      {/* 筛选和搜索 */}
      <Card className="bg-gray-800/50 border-gray-700">
        <div className="flex items-center gap-4 flex-wrap">
          <div className="flex gap-2">
            <Button
              type={filter === 'all' ? 'primary' : 'default'}
              onClick={() => setFilter('all')}
              className={
                filter === 'all'
                  ? 'bg-gradient-to-r from-orange-500 to-orange-600 border-none'
                  : 'border-gray-600 text-gray-300'
              }
            >
              全部
            </Button>
            <Button
              type={filter === 'unread' ? 'primary' : 'default'}
              onClick={() => setFilter('unread')}
              className={
                filter === 'unread'
                  ? 'bg-gradient-to-r from-orange-500 to-orange-600 border-none'
                  : 'border-gray-600 text-gray-300'
              }
            >
              未读
            </Button>
            <Button
              type={filter === 'starred' ? 'primary' : 'default'}
              onClick={() => setFilter('starred')}
              className={
                filter === 'starred'
                  ? 'bg-gradient-to-r from-orange-500 to-orange-600 border-none'
                  : 'border-gray-600 text-gray-300'
              }
            >
              收藏
            </Button>
          </div>
          <div className="flex-1 min-w-64">
            <Input
              placeholder="搜索通知..."
              prefix={<SearchOutlined className="text-gray-400" />}
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              className="bg-gray-700/50 border-gray-600"
              allowClear
            />
          </div>
        </div>
      </Card>

      {/* 通知列表 */}
      <Card className="bg-gray-800/50 border-gray-700">
        {filteredNotifications.length > 0 ? (
          <List
            dataSource={filteredNotifications}
            renderItem={(notification) => {
              const config = typeConfig[notification.type];
              const isUnread = !notification.isRead;

              return (
                <List.Item
                  className={`border-gray-700 hover:bg-gray-700/30 transition-all py-4 ${
                    isUnread ? 'bg-blue-900/10' : ''
                  }`}
                  actions={[
                    <Space key="actions" size="small">
                      {!notification.isRead && (
                        <Button
                          type="link"
                          size="small"
                          icon={<CheckCircleOutlined />}
                          onClick={() => markAsRead(notification.id)}
                          className="text-gray-400 hover:text-green-400"
                        >
                          已读
                        </Button>
                      )}
                      <Button
                        type="link"
                        size="small"
                        icon={<StarOutlined className={notification.isStarred ? 'text-yellow-400 fill-yellow-400' : ''} />}
                        onClick={() => toggleStar(notification.id)}
                        className="text-gray-400"
                      />
                      <Popconfirm
                        title="确认删除"
                        description="确定要删除这条通知吗？"
                        onConfirm={() => deleteNotification(notification.id)}
                        okText="确认"
                        cancelText="取消"
                      >
                        <Button
                          type="link"
                          size="small"
                          danger
                          icon={<DeleteOutlined />}
                          className="text-gray-400"
                        />
                      </Popconfirm>
                    </Space>,
                  ]}
                >
                  <List.Item.Meta
                    avatar={
                      <div
                        className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl ${config.bgClass}`}
                        style={{ color: config.color }}
                      >
                        {config.icon}
                      </div>
                    }
                    title={
                      <div className="flex items-center gap-2">
                        <span className={`font-medium ${isUnread ? 'text-white' : 'text-gray-300'}`}>
                          {notification.title}
                        </span>
                        {isUnread && (
                          <span className="w-2 h-2 bg-orange-500 rounded-full" />
                        )}
                        {notification.projectName && (
                          <Tag color="blue" className="ml-2">
                            {notification.projectName}
                          </Tag>
                        )}
                      </div>
                    }
                    description={
                      <div className="mt-2 space-y-2">
                        <p className="text-gray-400">{notification.content}</p>
                        <div className="flex items-center gap-4 text-xs text-gray-500">
                          <span className="flex items-center gap-1">
                            <ClockCircleOutlined />
                            {dayjs(notification.createdAt).fromNow()}
                          </span>
                        </div>
                      </div>
                    }
                  />
                </List.Item>
              );
            }}
          />
        ) : (
          <div className="py-16">
            <AppEmpty
              description={
                filter === 'unread'
                  ? '太棒了！没有未读消息'
                  : filter === 'starred'
                  ? '暂无收藏的消息'
                  : '暂无消息'
              }
              action={
                filter === 'unread' ? (
                  <Button
                    type="primary"
                    onClick={() => setFilter('all')}
                    className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
                  >
                    查看全部
                  </Button>
                ) : undefined
              }
            />
          </div>
        )}
      </Card>
    </div>
  );
}
