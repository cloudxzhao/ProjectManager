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
    <div className="min-h-screen bg-[var(--primary)]">
      {/* 背景图层 */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        {/* 网格图案 */}
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `
              linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
              linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px)
            `,
            backgroundSize: '50px 50px',
            opacity: 0.4,
          }}
        />
        {/* 渐变光效 */}
        <div
          className="absolute inset-0"
          style={{
            background: `
              radial-gradient(ellipse at 15% 30%, rgba(249, 115, 22, 0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 85% 70%, rgba(139, 92, 246, 0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 50% 50%, rgba(6, 182, 212, 0.04) 0%, transparent 60%)
            `,
          }}
        />
      </div>

      {/* 侧边栏 - 桌面端 */}
      <aside className="hidden lg:flex fixed left-0 top-0 bottom-0 w-64 glass-dark border-r border-white/8 z-100 flex-col">
        {/* Logo 区域 */}
        <div className="p-5 border-b border-white/8">
          <Link href="/dashboard" className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center glow-orange flex-shrink-0">
              <svg className="w-5.5 h-5.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
            </div>
            <div className="flex flex-col">
              <span className="font-display text-lg font-bold text-white tracking-tight">ProjectHub</span>
              <span className="text-xs text-gray-400">项目管理系统</span>
            </div>
          </Link>
        </div>

        {/* 导航菜单 */}
        <nav className="flex-1 p-3 space-y-0.5 overflow-y-auto">
          {menuItems.map((item) => {
            const isActive = activeKey === item.key || (item.key !== '/dashboard' && activeKey.startsWith(item.key));
            return (
              <Link
                key={item.key}
                href={item.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 group relative ${
                  isActive
                    ? 'bg-orange-500/10 text-orange-400'
                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                }`}
              >
                {/* 左侧激活指示条 */}
                <span
                  className={`absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-6 rounded-r-full transition-all duration-200 ${
                    isActive ? 'bg-orange-500' : 'bg-transparent group-hover:bg-orange-500/50'
                  }`}
                />
                <span className={`text-lg ${isActive ? 'text-orange-400' : 'text-gray-400 group-hover:text-white'}`}>
                  {item.icon}
                </span>
                <span className="font-medium text-sm">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* 底部用户信息 */}
        <div className="p-3 border-t border-white/8">
          <div className="p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-all cursor-pointer flex items-center gap-3">
            <Avatar
              src={user?.avatar}
              icon={<UserOutlined />}
              className="bg-gradient-to-br from-purple-400 to-pink-500"
              size={36}
            />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white truncate">{user?.username || '用户'}</p>
              <p className="text-xs text-gray-400 truncate">{user?.email || 'user@example.com'}</p>
            </div>
          </div>
        </div>
      </aside>

      {/* 移动端抽屉 */}
      <Drawer
        placement="left"
        onClose={() => setDrawerOpen(false)}
        open={drawerOpen}
        className="bg-[var(--primary)]"
        size="large"
      >
        <div className="flex flex-col h-full">
          {/* Logo 区域 */}
          <div className="p-5 border-b border-white/8">
            <Link href="/dashboard" className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center glow-orange">
                <svg className="w-5.5 h-5.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                  <polyline points="9 22 9 12 15 12 15 22" />
                </svg>
              </div>
              <span className="font-display text-lg font-bold text-white">ProjectHub</span>
            </Link>
          </div>

          {/* 导航菜单 */}
          <nav className="flex-1 p-3 space-y-0.5 overflow-y-auto">
            {menuItems.map((item) => {
              const isActive = activeKey === item.key || (item.key !== '/dashboard' && activeKey.startsWith(item.key));
              return (
                <Link
                  key={item.key}
                  href={item.path}
                  onClick={() => setDrawerOpen(false)}
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all ${
                    isActive
                      ? 'bg-orange-500/10 text-orange-400'
                      : 'text-gray-400 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <span className="text-lg">{item.icon}</span>
                  <span className="font-medium">{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      </Drawer>

      {/* 主内容区 */}
      <div className="relative z-1 ml-0 lg:ml-64 min-h-screen">
        {/* 顶部导航栏 */}
        <header className="sticky top-0 z-50 glass-dark border-b border-white/8">
          <div className="h-16 px-4 sm:px-6 flex items-center justify-between">
            {/* 左侧：菜单按钮和面包屑 */}
            <div className="flex items-center gap-4">
              <Button
                type="text"
                icon={<MenuOutlined className="text-lg" />}
                onClick={() => setDrawerOpen(true)}
                className="lg:hidden text-gray-400 hover:text-white"
              />

              {/* 面包屑导航 */}
              <nav className="hidden sm:flex items-center gap-2 text-sm">
                <Link href="/dashboard" className="text-gray-400 hover:text-orange-400 transition-colors">
                  <HomeOutlined />
                </Link>
                <span className="text-gray-600">/</span>
                <span className="text-gray-300 font-medium">工作台</span>
              </nav>
            </div>

            {/* 中间：搜索框 */}
            <div className="hidden md:flex flex-1 max-w-xl mx-8">
              <div className="relative w-full group">
                <SearchOutlined className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-orange-400 transition-colors" />
                <input
                  type="text"
                  placeholder="搜索项目、任务、文档... (Ctrl+K)"
                  className="w-full pl-10 pr-4 py-2.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-orange-500/50 focus:bg-white/10 focus:ring-2 focus:ring-orange-500/20 transition-all"
                />
              </div>
            </div>

            {/* 右侧：操作按钮 */}
            <div className="flex items-center gap-3">
              {/* 通知按钮 */}
              <Badge count={5} size="small" offset={[-3, 5]}>
                <button className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 text-gray-400 hover:text-white hover:bg-white/10 hover:border-orange-500/30 transition-all flex items-center justify-center">
                  <BellOutlined className="text-lg" />
                </button>
              </Badge>

              {/* 用户菜单 */}
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight" trigger={['click']}>
                <button className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-white/10 transition-all">
                  <Avatar
                    src={user?.avatar}
                    icon={<UserOutlined />}
                    className="bg-gradient-to-br from-orange-400 to-amber-500"
                    size={36}
                  />
                  <span className="text-sm text-gray-300 hidden sm:block">{user?.username || '用户'}</span>
                </button>
              </Dropdown>
            </div>
          </div>
        </header>

        {/* 页面内容 */}
        <main className="p-4 sm:p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
