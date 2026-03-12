'use client';

import { BackgroundLayer } from '@/components/common/BackgroundLayer';

interface AuthPageLayoutProps {
  children: React.ReactNode;
}

export default function AuthPageLayout({ children }: AuthPageLayoutProps) {
  return (
    <BackgroundLayer showShapes={true}>
      <div className="min-h-screen flex">
        {/* 左侧品牌区域 - 仅大屏显示 */}
        <div className="hidden lg:flex lg:w-1/2 relative items-center justify-center p-12">
          <div className="max-w-md text-white z-10 animate-fade-in-left">
            <h1 className="font-display text-5xl font-extrabold mb-6 leading-tight">
              高效协作<br />
              <span className="text-orange-400">项目驱动</span>
            </h1>
            <p className="text-white/70 text-lg mb-8 leading-relaxed">
              一站式项目管理解决方案，让团队协作更高效，让项目交付更可控。
            </p>
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>任务看板与进度追踪</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>团队协作与实时沟通</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 rounded-lg bg-orange-500/20 flex items-center justify-center text-orange-400">
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
                <span>数据报表与智能分析</span>
              </div>
            </div>
          </div>
        </div>

        {/* 右侧内容区域 */}
        <div className="flex-1 flex items-center justify-center p-4 sm:p-8">
          <div className="w-full max-w-[460px]">
            {children}
          </div>
        </div>
      </div>
    </BackgroundLayer>
  );
}
