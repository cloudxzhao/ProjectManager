'use client';

import React from 'react';
import { Card, Typography, Space, Tag, Button } from 'antd';
import { ArrowLeftOutlined, ArrowRightOutlined } from '@ant-design/icons';
import type { VersionDiff as VersionDiffType } from '@/types/wiki';
import styles from './VersionDiff.module.css';

const { Title, Text } = Typography;

interface VersionDiffProps {
  diff: VersionDiffType;
  onRestore?: (versionId: number) => void;
}

/**
 * 版本对比组件
 * 展示两个版本之间的差异
 */
export const VersionDiff: React.FC<VersionDiffProps> = ({ diff, onRestore }) => {
  const getLineClass = (type: string) => {
    switch (type) {
      case 'ADDED':
        return styles.added;
      case 'REMOVED':
        return styles.removed;
      default:
        return styles.unchanged;
    }
  };

  const getLinePrefix = (type: string) => {
    switch (type) {
      case 'ADDED':
        return '+';
      case 'REMOVED':
        return '-';
      default:
        return ' ';
    }
  };

  return (
    <div className={styles.container}>
      <Card className={styles.header}>
        <div className={styles.titleRow}>
          <Title level={4} className={styles.title}>
            {diff.title}
          </Title>
          <Space>
            <Tag color="blue">v{diff.oldVersion}</Tag>
            <ArrowRightOutlined />
            <Tag color="green">v{diff.newVersion}</Tag>
          </Space>
        </div>
        <div className={styles.stats}>
          <Tag color="green">+{diff.addedLines} 行</Tag>
          <Tag color="red">-{diff.removedLines} 行</Tag>
        </div>
      </Card>

      <Card className={styles.diffContainer}>
        <div className={styles.diffLines}>
          {diff.diffLines?.map((line, index) => (
            <div key={index} className={`${styles.diffLine} ${getLineClass(line.type)}`}>
              <span className={styles.lineNumber}>
                {line.oldLineNumber > 0 ? line.oldLineNumber : ''}
              </span>
              <span className={styles.lineNumber}>
                {line.newLineNumber > 0 ? line.newLineNumber : ''}
              </span>
              <span className={styles.linePrefix}>{getLinePrefix(line.type)}</span>
              <span className={styles.lineContent}>{line.content}</span>
            </div>
          ))}
        </div>
      </Card>

      {onRestore && (
        <div className={styles.actions}>
          <Button
            type="primary"
            onClick={() => onRestore(diff.oldVersion)}
            disabled={diff.newVersion === diff.oldVersion}
          >
            恢复到 v{diff.oldVersion}
          </Button>
        </div>
      )}
    </div>
  );
};

export default VersionDiff;