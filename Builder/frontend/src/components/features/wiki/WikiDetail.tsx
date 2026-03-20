'use client';

import React from 'react';
import { Card, Typography, Tag, Space, Button, Tooltip } from 'antd';
import { EyeOutlined, EditOutlined, HistoryOutlined } from '@ant-design/icons';
import type { WikiDetail as WikiDetailType } from '@/types/wiki';
import styles from './WikiDetail.module.css';

const { Title, Text, Paragraph } = Typography;

interface WikiDetailProps {
  wiki: WikiDetailType;
  onEdit?: (id: number) => void;
  onVersionHistory?: (id: number) => void;
}

/**
 * Wiki 文档详情组件
 * 用于展示 Wiki 文档的详细内容，支持 Markdown/HTML 渲染
 */
export const WikiDetail: React.FC<WikiDetailProps> = ({
  wiki,
  onEdit,
  onVersionHistory,
}) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return 'green';
      case 'DRAFT':
        return 'orange';
      case 'ARCHIVED':
        return 'default';
      default:
        return 'blue';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return '已发布';
      case 'DRAFT':
        return '草稿';
      case 'ARCHIVED':
        return '已归档';
      default:
        return status;
    }
  };

  return (
    <Card
      className={styles.container}
      title={
        <div className={styles.header}>
          <Title level={4} className={styles.title}>
            {wiki.title}
          </Title>
          <Space>
            <Tag color={getStatusColor(wiki.status)}>{getStatusText(wiki.status)}</Tag>
            <Tag>v{wiki.version}</Tag>
          </Space>
        </div>
      }
      extra={
        <Space>
          <Tooltip title="浏览次数">
            <span className={styles.viewCount}>
              <EyeOutlined /> {wiki.viewCount}
            </span>
          </Tooltip>
          {onVersionHistory && (
            <Button
              type="text"
              icon={<HistoryOutlined />}
              onClick={() => onVersionHistory(wiki.id)}
            >
              历史版本
            </Button>
          )}
          {onEdit && (
            <Button type="text" icon={<EditOutlined />} onClick={() => onEdit(wiki.id)}>
              编辑
            </Button>
          )}
        </Space>
      }
    >
      {wiki.summary && (
        <Paragraph type="secondary" className={styles.summary}>
          {wiki.summary}
        </Paragraph>
      )}

      <div
        className={styles.content}
        dangerouslySetInnerHTML={{ __html: wiki.contentHtml || wiki.content }}
      />

      <div className={styles.footer}>
        <Text type="secondary" className={styles.meta}>
          作者：{wiki.authorName || '未知'} |
          创建于：{new Date(wiki.createdAt).toLocaleString('zh-CN')}
          {wiki.updatedAt && ` | 更新于：${new Date(wiki.updatedAt).toLocaleString('zh-CN')}`}
        </Text>
      </div>
    </Card>
  );
};

export default WikiDetail;