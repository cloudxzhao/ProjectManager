'use client';

import React from 'react';

/**
 * 背景图层组件
 * 实现深色玻璃态设计的背景效果
 */
export const BackgroundLayer: React.FC<{
  children?: React.ReactNode;
  showGrid?: boolean;
  showShapes?: boolean;
  overlayOpacity?: number;
}> = ({ children, showGrid = true, showShapes = true, overlayOpacity = 0.85 }) => {
  return (
    <div className="relative min-h-screen">
      {/* 背景图层 */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        {/* 背景图片 */}
        <img
          src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80"
          alt=""
          className="w-full h-full object-cover"
          style={{
            filter: 'brightness(0.35) saturate(1.1)',
          }}
        />

        {/* 渐变遮罩 */}
        <div
          className="absolute inset-0"
          style={{
            background: `
              linear-gradient(135deg, rgba(15, 23, 42, ${overlayOpacity}) 0%, rgba(15, 23, 42, ${overlayOpacity - 0.1}) 50%, rgba(15, 23, 42, ${overlayOpacity}) 100%),
              linear-gradient(45deg, transparent 40%, rgba(249, 115, 22, 0.06) 100%)
            `,
          }}
        />

        {/* 网格图案 */}
        {showGrid && (
          <div
            className="absolute inset-0"
            style={{
              backgroundImage: `
                linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
                linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px)
              `,
              backgroundSize: '60px 60px',
              opacity: 0.5,
            }}
          />
        )}

        {/* 浮动几何图形 */}
        {showShapes && (
          <div className="absolute inset-0 overflow-hidden">
            {/* 方形 */}
            <div
              className="absolute top-[5%] left-[3%] w-[300px] h-[300px] border border-orange-500/20 animate-float"
              style={{
                transform: 'rotate(45deg)',
                animationDelay: '0s',
              }}
            />

            {/* 圆形 - 青色 */}
            <div
              className="absolute top-[55%] left-[10%] w-[180px] h-[180px] rounded-full border border-cyan-500/20 animate-float"
              style={{
                animationDelay: '-8s',
              }}
            />

            {/* 方形 - 紫色 */}
            <div
              className="absolute top-[25%] right-[8%] w-[120px] h-[120px] border border-purple-500/20 animate-float"
              style={{
                animationDelay: '-15s',
              }}
            />

            {/* 圆形 - 青柠色 */}
            <div
              className="absolute bottom-[10%] left-[20%] w-[200px] h-[200px] rounded-full border border-lime-500/20 animate-float"
              style={{
                animationDelay: '-20s',
              }}
            />
          </div>
        )}
      </div>

      {/* 内容区域 */}
      <div className="relative z-10">{children}</div>
    </div>
  );
};

export default BackgroundLayer;
