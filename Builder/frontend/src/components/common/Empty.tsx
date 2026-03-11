'use client';

import { Empty as AntEmpty, Button } from 'antd';
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import Link from 'next/link';

interface EmptyProps {
  description?: string;
  image?: React.ReactNode;
  action?: 'back' | 'home' | 'refresh' | React.ReactNode;
  onAction?: () => void;
  className?: string;
}

export const Empty: React.FC<EmptyProps> = ({
  description = '暂无数据',
  image,
  action = 'back',
  onAction,
  className = '',
}) => {
  const renderAction = () => {
    if (action === 'back') {
      return (
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={onAction || (() => window.history.back())}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none text-white"
        >
          返回上一页
        </Button>
      );
    }

    if (action === 'home') {
      return (
        <Link href="/">
          <Button
            icon={<HomeOutlined />}
            type="primary"
            className="bg-gradient-to-r from-orange-500 to-orange-600 border-none"
          >
            返回首页
          </Button>
        </Link>
      );
    }

    if (action === 'refresh') {
      return (
        <Button
          onClick={onAction || (() => window.location.reload())}
          className="bg-gradient-to-r from-orange-500 to-orange-600 border-none text-white"
        >
          刷新页面
        </Button>
      );
    }

    return action;
  };

  return (
    <div className={`flex flex-col items-center justify-center py-16 ${className}`}>
      <AntEmpty
        image={image}
        description={
          <span className="text-gray-400 text-lg">{description}</span>
        }
      />
      <div className="mt-8">{renderAction()}</div>
    </div>
  );
};
