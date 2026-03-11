'use client';

import { Skeleton as AntSkeleton } from 'antd';

interface SkeletonProps {
  variant?: 'card' | 'list' | 'table' | 'dashboard';
  count?: number;
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({
  variant = 'card',
  count = 1,
  className = '',
}) => {
  if (variant === 'card') {
    return (
      <div className={`space-y-4 ${className}`}>
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg">
            <AntSkeleton.Avatar active rounded size={40} className="mb-3" />
            <AntSkeleton.Input active className="mb-2" style={{ width: '60%' }} />
            <AntSkeleton.Input active className="mb-2" />
            <AntSkeleton.Input active style={{ width: '80%' }} />
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'list') {
    return (
      <div className={`space-y-3 ${className}`}>
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="flex items-center gap-3 p-3 bg-gray-800/50 border border-gray-700 rounded-lg">
            <AntSkeleton.Avatar active rounded size={36} />
            <div className="flex-1 space-y-2">
              <AntSkeleton.Input active style={{ width: '40%' }} />
              <AntSkeleton.Input active style={{ width: '70%' }} />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'table') {
    return (
      <div className={`space-y-3 ${className}`}>
        <div className="flex gap-2 mb-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <AntSkeleton.Input key={i} active className="flex-1" />
          ))}
        </div>
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="flex gap-2 p-3 bg-gray-800/50 border border-gray-700 rounded-lg">
            {Array.from({ length: 4 }).map((_, j) => (
              <AntSkeleton.Input key={j} active className="flex-1" />
            ))}
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'dashboard') {
    return (
      <div className={`space-y-6 ${className}`}>
        {/* 统计卡片骨架 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg">
              <AntSkeleton.Avatar active rounded size={32} className="mb-3" />
              <AntSkeleton.Input active className="mb-2" style={{ width: '50%' }} />
              <AntSkeleton.Input active style={{ width: '70%' }} />
            </div>
          ))}
        </div>
        {/* 主要内容骨架 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg">
                <AntSkeleton.Input active className="mb-2" style={{ width: '60%' }} />
                <AntSkeleton.Input active style={{ width: '80%' }} />
              </div>
            ))}
          </div>
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="p-4 bg-gray-800/50 border border-gray-700 rounded-lg">
                <AntSkeleton.Input active className="mb-2" style={{ width: '50%' }} />
                <AntSkeleton.Input active style={{ width: '70%' }} />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return null;
};
