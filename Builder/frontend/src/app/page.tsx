'use client';

import React from 'react';
import Link from 'next/link';
import { BackgroundLayer } from '@/components/common/BackgroundLayer';
import { Button } from '@/components/ui/Buttons';

/**
 * 首页 - Landing Page
 * 项目管理系统的主页
 */
export default function HomePage() {
  return (
    <BackgroundLayer>
      {/* 导航栏 */}
      <nav className="fixed top-0 left-0 right-0 z-100 px-6 lg:px-12 py-4 glass-dark border-b border-white/10">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-3">
            <div className="w-11 h-11 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center glow-orange">
              <svg className="w-7 h-7 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
            </div>
            <div className="flex flex-col">
              <span className="font-display text-xl font-bold text-white tracking-tight">ProjectHub</span>
              <span className="text-xs text-gray-400">项目管理系统</span>
            </div>
          </Link>

          {/* 导航链接 */}
          <ul className="hidden lg:flex items-center gap-8 list-none">
            <li>
              <a href="#features" className="text-gray-400 hover:text-white transition-colors relative group">
                功能特性
                <span className="absolute bottom--4 left-0 w-0 h-0.5 bg-orange-500 transition-all group-hover:w-full" />
              </a>
            </li>
            <li>
              <a href="#pricing" className="text-gray-400 hover:text-white transition-colors relative group">
                定价方案
                <span className="absolute bottom--4 left-0 w-0 h-0.5 bg-orange-500 transition-all group-hover:w-full" />
              </a>
            </li>
            <li>
              <a href="#docs" className="text-gray-400 hover:text-white transition-colors relative group">
                文档中心
                <span className="absolute bottom--4 left-0 w-0 h-0.5 bg-orange-500 transition-all group-hover:w-full" />
              </a>
            </li>
            <li>
              <a href="#about" className="text-gray-400 hover:text-white transition-colors relative group">
                关于我们
                <span className="absolute bottom--4 left-0 w-0 h-0.5 bg-orange-500 transition-all group-hover:w-full" />
              </a>
            </li>
          </ul>

          {/* 操作按钮 */}
          <div className="hidden sm:flex items-center gap-3">
            <Link href="/login">
              <Button variant="secondary" size="sm">登录</Button>
            </Link>
            <Link href="/register">
              <Button variant="primary" size="sm">免费试用</Button>
            </Link>
          </div>

          {/* 移动端菜单按钮 */}
          <button className="lg:hidden flex flex-col gap-1.5 p-2 text-white">
            <span className="w-6 h-0.5 bg-white transition-all" />
            <span className="w-6 h-0.5 bg-white transition-all" />
            <span className="w-6 h-0.5 bg-white transition-all" />
          </button>
        </div>
      </nav>

      {/* Hero 区域 */}
      <section className="relative z-1 min-h-screen flex flex-col items-center justify-center px-4 pt-20 pb-12 text-center">
        {/* 新版本徽章 */}
        <div className="inline-flex items-center gap-2 px-4 py-2 mb-8 rounded-full bg-orange-500/10 border border-orange-500/30 text-orange-400 font-medium text-sm animate-fade-in-down">
          <span className="w-2 h-2 bg-orange-500 rounded-full animate-pulse" />
          全新版本 3.0 已发布
        </div>

        {/* 主标题 */}
        <h1 className="font-display text-5xl sm:text-6xl lg:text-7xl font-bold leading-tight mb-6 animate-fade-in-up">
          让团队协作更<br />
          <span className="text-gradient">高效 · 智能 · 可控</span>
        </h1>

        {/* 描述文字 */}
        <p className="text-gray-400 text-lg sm:text-xl max-w-2xl mb-10 leading-relaxed animate-fade-in-up" style={{ animationDelay: '0.1s' }}>
          一站式项目管理解决方案，整合敏捷开发、任务追踪、知识沉淀与数据分析，
          帮助团队提升 300% 的交付效率。
        </p>

        {/* 操作按钮 */}
        <div className="flex flex-wrap gap-4 justify-center animate-fade-in-up" style={{ animationDelay: '0.2s' }}>
          <Link href="/register">
            <Button variant="primary" size="lg" rightIcon={
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="5" y1="12" x2="19" y2="12" />
                <polyline points="12 5 19 12 12 19" />
              </svg>
            }>
              立即开始
            </Button>
          </Link>
          <Link href="#demo">
            <Button variant="secondary" size="lg" leftIcon={
              <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="5 3 19 12 5 21 5 3" />
              </svg>
            }>
              观看演示
            </Button>
          </Link>
        </div>
      </section>

      {/* 功能特性区域 */}
      <section id="features" className="relative z-1 px-4 py-20 max-w-7xl mx-auto">
        {/* 区域标题 */}
        <div className="text-center mb-16">
          <span className="inline-block text-orange-400 text-sm font-bold uppercase tracking-widest mb-4">
            核心功能
          </span>
          <h2 className="font-display text-4xl sm:text-5xl font-bold mb-4">
            六大模块，赋能全流程管理
          </h2>
          <p className="text-gray-400 text-lg max-w-2xl mx-auto">
            从需求到交付，从协作到沉淀，ProjectHub 提供完整的项目管理工具链。
          </p>
        </div>

        {/* 功能卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* 敏捷看板 */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <line x1="9" y1="3" x2="9" y2="21" />
                <line x1="15" y1="3" x2="15" y2="21" />
              </svg>
            }
            title="敏捷看板"
            description="可视化任务管理，支持 Scrum 和 Kanban 方法论。拖拽式操作，实时同步团队进度，让每个迭代都清晰可控。"
            tags={['Scrum', 'Kanban', 'Sprint']}
            accentColor="orange"
          />

          {/* 用户故事 */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 20h9" />
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
              </svg>
            }
            title="用户故事"
            description="以用户视角定义需求，结构化拆解史诗与故事点。支持验收标准、优先级排序和依赖关系管理。"
            tags={['Epic', 'Story', 'Backlog']}
            accentColor="purple"
          />

          {/* 问题追踪 */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            }
            title="问题追踪"
            description="Bug、缺陷、障碍全生命周期管理。自动流转状态，智能分配负责人，确保问题及时闭环。"
            tags={['Bug', 'Issue', 'Workflow']}
            accentColor="pink"
          />

          {/* 项目 Wiki */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                <line x1="8" y1="7" x2="16" y2="7" />
                <line x1="8" y1="11" x2="14" y2="11" />
              </svg>
            }
            title="项目 Wiki"
            description="团队知识库与文档中心。支持 Markdown、富文本、代码块，版本历史自动保存，让知识有序沉淀。"
            tags={['文档', 'Markdown', '版本控制']}
            accentColor="cyan"
          />

          {/* 数据分析 */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="20" x2="18" y2="10" />
                <line x1="12" y1="20" x2="12" y2="4" />
                <line x1="6" y1="20" x2="6" y2="14" />
              </svg>
            }
            title="数据分析"
            description="燃尽图、累积流、速度图表等敏捷度量。多维度报表洞察团队效能，数据驱动持续改进。"
            tags={['燃尽图', '报表', '效能']}
            accentColor="lime"
          />

          {/* 生态集成 */}
          <FeatureCard
            icon={
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
            }
            title="生态集成"
            description="无缝连接 GitLab、GitHub、Jenkins、Slack 等工具。开放 API 支持自定义集成，打造一站式工作台。"
            tags={['Git', 'CI/CD', 'API']}
            accentColor="blue"
          />
        </div>
      </section>

      {/* CTA 区域 */}
      <section className="relative z-1 px-4 py-20">
        <div className="max-w-4xl mx-auto">
          <div className="relative rounded-3xl overflow-hidden bg-gradient-to-br from-orange-500/20 to-purple-500/10 border border-orange-500/30 p-12 sm:p-16 text-center">
            {/* 旋转光效 */}
            <div
              className="absolute inset-0 opacity-30"
              style={{
                background: 'radial-gradient(circle, rgba(249, 115, 22, 0.1) 0%, transparent 70%)',
                animation: 'rotate 30s linear infinite',
              }}
            />

            <div className="relative z-10">
              <h2 className="font-display text-3xl sm:text-4xl font-bold mb-4">
                准备好提升团队效率了吗？
              </h2>
              <p className="text-gray-400 text-lg mb-8 max-w-xl mx-auto">
                加入 10,000+ 团队，开始使用 ProjectHub 管理您的下一个项目。
              </p>
              <div className="flex flex-wrap gap-4 justify-center">
                <Link href="/register">
                  <Button variant="primary" size="lg">免费开始使用</Button>
                </Link>
                <Link href="#contact">
                  <Button variant="secondary" size="lg">联系销售团队</Button>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 页脚 */}
      <footer className="relative z-1 border-t border-white/10 py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <ul className="flex flex-wrap justify-center gap-8 mb-8 list-none">
            <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">产品文档</a></li>
            <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">API 参考</a></li>
            <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">隐私政策</a></li>
            <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">服务条款</a></li>
            <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">联系我们</a></li>
          </ul>
          <p className="text-center text-gray-500 text-sm">
            &copy; 2026 ProjectHub. All rights reserved.
          </p>
        </div>
      </footer>
    </BackgroundLayer>
  );
}

/**
 * 功能卡片组件
 */
interface FeatureCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  tags: string[];
  accentColor: 'orange' | 'purple' | 'pink' | 'cyan' | 'lime' | 'blue';
}

const FeatureCard: React.FC<FeatureCardProps> = ({
  icon,
  title,
  description,
  tags,
  accentColor,
}) => {
  const accentStyles = {
    orange: 'from-orange-500 to-orange-600 glow-orange',
    purple: 'from-purple-500 to-purple-600 glow-purple',
    pink: 'from-pink-500 to-pink-600',
    cyan: 'from-cyan-500 to-cyan-600 glow-cyan',
    lime: 'from-lime-500 to-lime-600',
    blue: 'from-blue-500 to-blue-600 glow-orange',
  };

  return (
    <div className="group relative p-6 bg-white/3 border border-white/8 rounded-2xl transition-all duration-500 hover:-translate-y-2 hover:bg-white/5 hover:shadow-2xl cursor-pointer overflow-hidden">
      {/* 顶部渐变条 */}
      <div className={`absolute top-0 left-0 right-0 h-1 bg-gradient-to-r ${accentStyles[accentColor].split(' ')[0]} ${accentStyles[accentColor].split(' ')[1]} opacity-60 group-hover:opacity-100 transition-opacity`} />

      {/* 图标 */}
      <div className={`w-14 h-14 rounded-xl bg-gradient-to-br ${accentStyles[accentColor]} flex items-center justify-center mb-6 glow-orange`}>
        <div className="w-7 h-7 text-white">
          {icon}
        </div>
      </div>

      {/* 标题 */}
      <h3 className="font-display text-xl font-bold text-white mb-3">
        {title}
      </h3>

      {/* 描述 */}
      <p className="text-gray-400 text-sm leading-relaxed mb-4">
        {description}
      </p>

      {/* 标签 */}
      <div className="flex flex-wrap gap-2">
        {tags.map((tag) => (
          <span
            key={tag}
            className="px-3 py-1.5 bg-white/5 border border-white/8 rounded-lg text-xs text-gray-400 group-hover:border-orange-500/50 group-hover:text-white transition-all"
          >
            {tag}
          </span>
        ))}
      </div>
    </div>
  );
};
