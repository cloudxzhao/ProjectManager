'use client';

import { Result, Button } from 'antd';
import { HomeOutlined, ArrowLeftOutlined, SyncOutlined, CustomerServiceOutlined } from '@ant-design/icons';
import Link from 'next/link';

interface ErrorFallbackProps {
  error?: Error;
  reset?: () => void;
  status?: '404' | '403' | '500';
  title?: string;
  subTitle?: string;
}

export const ErrorFallback: React.FC<ErrorFallbackProps> = ({
  error,
  reset,
  status = '500',
  title,
  subTitle,
}) => {
  const config = {
    '404': {
      title: title || '404',
      subTitle: subTitle || '抱歉，您访问的页面不存在',
      icon: '🔍',
      color: 'from-blue-400 to-cyan-500',
    },
    '403': {
      title: title || '403',
      subTitle: subTitle || '抱歉，您没有权限访问此页面',
      icon: '🔒',
      color: 'from-amber-400 to-orange-500',
    },
    '500': {
      title: title || '500',
      subTitle: subTitle || '服务器遇到问题了，我们的团队正在紧急处理中',
      icon: '⚡',
      color: 'from-red-400 to-pink-500',
    },
  };

  const currentConfig = config[status];

  // 根据错误信息提供更友好的提示
  const getFriendlyMessage = (error?: Error) => {
    if (!error) return null;

    const message = error.message.toLowerCase();

    if (message.includes('network') || message.includes('fetch')) {
      return '网络连接失败，请检查网络设置或联系管理员';
    }
    if (message.includes('timeout')) {
      return '请求超时，服务器响应时间过长，请稍后重试';
    }
    if (message.includes('unauthorized') || message.includes('401')) {
      return '登录已过期，请重新登录后再试';
    }
    if (message.includes('forbidden') || message.includes('403')) {
      return '您没有权限执行此操作';
    }
    if (message.includes('not found') || message.includes('404')) {
      return '请求的资源不存在';
    }
    if (message.includes('500') || message.includes('internal')) {
      return '服务器内部错误，技术团队已收到告警，正在排查中';
    }

    return error.message;
  };

  const friendlyMessage = getFriendlyMessage(error);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
      <div className="text-center px-4 max-w-2xl">
        <div className="text-9xl mb-8">{currentConfig.icon}</div>
        <h1 className="text-6xl font-bold text-gradient mb-4">{currentConfig.title}</h1>
        <p className="text-xl text-gray-300 mb-4">{currentConfig.subTitle}</p>

        {friendlyMessage && (
          <div className="mb-8 p-4 bg-gray-800/60 border border-gray-700/50 rounded-xl">
            <p className="text-gray-300 text-sm leading-relaxed">{friendlyMessage}</p>
          </div>
        )}

        {/* 调试信息 - 开发环境显示 */}
        {process.env.NODE_ENV === 'development' && error && (
          <details className="max-w-md mx-auto mb-8 text-left">
            <summary className="cursor-pointer text-xs text-gray-500 hover:text-gray-400 mb-2">
              技术详情（仅开发环境可见）
            </summary>
            <div className="p-4 bg-gray-900/80 border border-gray-700 rounded-lg">
              <p className="text-xs text-red-400 font-mono break-all">{error.message}</p>
              {error.stack && (
                <pre className="mt-2 text-xs text-gray-500 font-mono whitespace-pre-wrap max-h-40 overflow-y-auto">
                  {error.stack}
                </pre>
              )}
            </div>
          </details>
        )}

        <div className="flex gap-4 justify-center flex-wrap">
          <Link href="/">
            <Button
              icon={<HomeOutlined />}
              type="primary"
              size="large"
              className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
            >
              返回首页
            </Button>
          </Link>
          {reset && (
            <Button
              size="large"
              onClick={reset}
              icon={<SyncOutlined />}
              className="border-gray-600 text-gray-300 hover:border-orange-500/50 hover:text-orange-400 transition-all"
            >
              重试
            </Button>
          )}
          <Button
            size="large"
            onClick={() => window.history.back()}
            icon={<ArrowLeftOutlined />}
            className="border-gray-600 text-gray-300 hover:border-orange-500/50 hover:text-orange-400 transition-all"
          >
            返回上一页
          </Button>
        </div>

        <div className="mt-8 flex items-center justify-center gap-2 text-gray-500 text-sm">
          <CustomerServiceOutlined />
          <span>如问题持续，请联系技术支持或查看系统状态页面</span>
        </div>
      </div>
    </div>
  );
};
