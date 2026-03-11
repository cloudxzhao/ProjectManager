'use client';

import { Result, Button } from 'antd';
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons';
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
    },
    '403': {
      title: title || '403',
      subTitle: subTitle || '抱歉，您没有权限访问此页面',
      icon: '🔒',
    },
    '500': {
      title: title || '500',
      subTitle: subTitle || '服务器开小差了，请稍后再试',
      icon: '⚠️',
    },
  };

  const currentConfig = config[status];

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
      <div className="text-center px-4">
        <div className="text-9xl mb-8">{currentConfig.icon}</div>
        <h1 className="text-6xl font-bold text-gradient mb-4">{currentConfig.title}</h1>
        <p className="text-xl text-gray-400 mb-8">{currentConfig.subTitle}</p>

        {error && (
          <div className="max-w-md mx-auto mb-8 p-4 bg-gray-800/50 border border-gray-700 rounded-lg text-left">
            <p className="text-sm text-red-400 font-mono">{error.message}</p>
          </div>
        )}

        <div className="flex gap-4 justify-center">
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
              className="border-gray-600 text-gray-300"
            >
              重试
            </Button>
          )}
          <Button
            size="large"
            onClick={() => window.history.back()}
            icon={<ArrowLeftOutlined />}
            className="border-gray-600 text-gray-300"
          >
            返回上一页
          </Button>
        </div>
      </div>
    </div>
  );
};
