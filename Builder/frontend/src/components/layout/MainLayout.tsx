'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/lib/hooks/useAuth';
import { Avatar, Dropdown, MenuProps, Badge, Drawer, Button } from 'antd';
import {
  HomeOutlined,
  ProjectOutlined,
  CheckCircleOutlined,
  BookOutlined,
  BarChartOutlined,
  SettingOutlined,
  MenuOutlined,
  BellOutlined,
  SearchOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';

interface MainLayoutProps {
  children: React.ReactNode;
}

// 侧边栏菜单项
const menuItems = [
  { key: '/dashboard', icon: <HomeOutlined />, label: '工作台', path: '/dashboard' },
  { key: '/projects', icon: <ProjectOutlined />, label: '项目', path: '/projects' },
  { key: '/tasks', icon: <CheckCircleOutlined />, label: '任务', path: '/tasks' },
  { key: '/messages', icon: <BellOutlined />, label: '消息', path: '/messages' },
  { key: '/wiki', icon: <BookOutlined />, label: '知识库', path: '/wiki' },
  { key: '/reports', icon: <BarChartOutlined />, label: '报表', path: '/reports' },
  { key: '/settings', icon: <SettingOutlined />, label: '设置', path: '/settings' },
];

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [drawerOpen, setDrawerOpen] = useState(false);

  // 用户菜单
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: <Link href="/settings">个人中心</Link>,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: logout,
    },
  ];

  // 当前激活的菜单项
  const activeKey = pathname || '/dashboard';

  return (
    <div className="min-h-screen bg-gray-900">
      {/* 顶部导航栏 */}
      <header className="fixed top-0 left-0 right-0 h-16 bg-gray-800/80 backdrop-blur-md border-b border-gray-700 z-50">
        <div className="h-full px-4 flex items-center justify-between">
          {/* 左侧：Logo 和菜单 */}
          <div className="flex items-center gap-4">
            <Button
              type="text"
              icon={<MenuOutlined className="text-lg" />}
              onClick={() => setDrawerOpen(true)}
              className="lg:hidden text-gray-400 hover:text-white"
            />
            <Link href="/dashboard" className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-amber-500 rounded-xl flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                  />
                </svg>
              </div>
              <span className="text-xl font-bold text-white hidden sm:block">ProjectHub</span>
            </Link>
          </div>

          {/* 中间：搜索框 */}
          <div className="hidden md:flex flex-1 max-w-xl mx-8">
            <div className="relative w-full">
              <SearchOutlined className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="搜索项目、任务、文档... (Ctrl+K)"
                className="w-full pl-10 pr-4 py-2.5 bg-gray-700/50 border border-gray-600 rounded-lg text-gray-200 placeholder-gray-400 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-all"
              />
            </div>
          </div>

          {/* 右侧：通知和用户 */}
          <div className="flex items-center gap-4">
            {/* 通知 */}
            <Badge count={5} size="small" offset={[-5, 5]}>
              <button className="p-2 text-gray-400 hover:text-white transition-colors relative">
                <BellOutlined className="text-xl" />
              </button>
            </Badge>

            {/* 用户菜单 */}
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight" trigger={['click']}>
              <button className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-gray-700/50 transition-colors">
                <Avatar
                  src={user?.avatar}
                  icon={<UserOutlined />}
                  className="bg-gradient-to-br from-orange-400 to-amber-500"
                  size={32}
                />
                <span className="text-sm text-gray-200 hidden sm:block">{user?.username || '用户'}</span>
              </button>
            </Dropdown>
          </div>
        </div>
      </header>

      {/* 侧边栏 - 桌面端 */}
      <aside className="hidden lg:flex fixed left-0 top-16 bottom-0 w-64 bg-gray-800/50 backdrop-blur-sm border-r border-gray-700 flex-col">
        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {menuItems.map((item) => {
            const isActive = activeKey === item.key || (item.key !== '/dashboard' && activeKey.startsWith(item.key));
            return (
              <Link
                key={item.key}
                href={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                  isActive
                    ? 'bg-gradient-to-r from-orange-500/20 to-amber-500/20 text-orange-400 border border-orange-500/30'
                    : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
                }`}
              >
                <span className="text-lg">{item.icon}</span>
                <span className="font-medium">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* 底部：项目信息 */}
        <div className="p-4 border-t border-gray-700">
          <div className="px-4 py-3 bg-gray-700/30 rounded-lg">
            <p className="text-xs text-gray-400 mb-1">当前版本</p>
            <p className="text-sm text-white font-medium">v1.0.0</p>
          </div>
        </div>
      </aside>

      {/* 移动端抽屉 */}
      <Drawer
        placement="left"
        onClose={() => setDrawerOpen(false)}
        open={drawerOpen}
        className="bg-gray-800"
        width={280}
      >
        <nav className="space-y-1">
          {menuItems.map((item) => {
            const isActive = activeKey === item.key || (item.key !== '/dashboard' && activeKey.startsWith(item.key));
            return (
              <Link
                key={item.key}
                href={item.path}
                onClick={() => setDrawerOpen(false)}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                  isActive
                    ? 'bg-gradient-to-r from-orange-500/20 to-amber-500/20 text-orange-400 border border-orange-500/30'
                    : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
                }`}
              >
                <span className="text-lg">{item.icon}</span>
                <span className="font-medium">{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </Drawer>

      {/* 主内容区 */}
      <main className="pt-16 lg:pl-64 min-h-screen">
        <div className="p-4 sm:p-6 lg:p-8">{children}</div>
      </main>
    </div>
  );
};

export default MainLayout;
