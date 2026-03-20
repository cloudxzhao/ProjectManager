'use client';

import React, { useState } from 'react';
import { Table, Button, Space, Tag, Modal, message } from 'antd';
import { HistoryOutlined, SwapOutlined, RollbackOutlined } from '@ant-design/icons';
import type { WikiVersion } from '@/types/wiki';
import { diffWikiVersions, restoreWikiVersion } from '@/lib/api/wiki';
import VersionDiff from './VersionDiff';
import styles from './VersionList.module.css';

interface VersionListProps {
  projectId: number;
  wikiId: number;
  versions: WikiVersion[];
  onRefresh?: () => void;
  loading?: boolean;
}

/**
 * 版本历史列表组件
 */
export const VersionList: React.FC<VersionListProps> = ({
  projectId,
  wikiId,
  versions,
  onRefresh,
  loading = false,
}) => {
  const [diffModalVisible, setDiffModalVisible] = useState(false);
  const [selectedVersion, setSelectedVersion] = useState<any>(null);
  const [compareVersion, setCompareVersion] = useState<any>(null);
  const [diffData, setDiffData] = useState<any>(null);
  const [diffLoading, setDiffLoading] = useState(false);

  const getChangeTypeColor = (type: string) => {
    switch (type) {
      case 'CREATE':
        return 'green';
      case 'UPDATE':
        return 'blue';
      case 'RESTORE':
        return 'orange';
      default:
        return 'default';
    }
  };

  const getChangeTypeText = (type: string) => {
    switch (type) {
      case 'CREATE':
        return '创建';
      case 'UPDATE':
        return '更新';
      case 'RESTORE':
        return '恢复';
      default:
        return type;
    }
  };

  const handleCompare = async (record: WikiVersion) => {
    // 找到当前版本进行比较
    const currentVersion = versions.find((v) => !v.id) || versions[0];
    if (currentVersion) {
      setSelectedVersion(currentVersion);
      setCompareVersion(record);
      setDiffLoading(true);
      setDiffModalVisible(true);

      try {
        const diff = await diffWikiVersions(
          projectId,
          wikiId,
          record.version,
          currentVersion.version
        );
        setDiffData(diff);
      } catch (error) {
        message.error('获取版本差异失败');
      } finally {
        setDiffLoading(false);
      }
    }
  };

  const handleRestore = async (record: WikiVersion) => {
    Modal.confirm({
      title: '确认恢复',
      content: `确定要恢复到 v${record.version} 版本吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await restoreWikiVersion(projectId, wikiId, record.version, `恢复到 v${record.version}`);
          message.success('版本恢复成功');
          onRefresh?.();
        } catch (error) {
          message.error('版本恢复失败');
        }
      },
    });
  };

  const columns = [
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
      render: (version: number, record: WikiVersion) => (
        <Tag color={record.id ? 'blue' : 'green'}>v{version}</Tag>
      ),
    },
    {
      title: '变更类型',
      dataIndex: 'changeType',
      key: 'changeType',
      width: 100,
      render: (type: string) => (
        <Tag color={getChangeTypeColor(type)}>{getChangeTypeText(type)}</Tag>
      ),
    },
    {
      title: '变更说明',
      dataIndex: 'changeLog',
      key: 'changeLog',
      ellipsis: true,
    },
    {
      title: '修改人',
      dataIndex: 'userName',
      key: 'userName',
      width: 120,
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: WikiVersion) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<SwapOutlined />}
            onClick={() => handleCompare(record)}
            disabled={!record.id}
          >
            对比
          </Button>
          <Button
            type="link"
            size="small"
            icon={<RollbackOutlined />}
            onClick={() => handleRestore(record)}
            disabled={!record.id}
          >
            恢复
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Table
        dataSource={versions}
        columns={columns}
        rowKey={(record) => `${record.id || 'current'}-${record.version}`}
        pagination={false}
        loading={loading}
        size="small"
      />

      <Modal
        title={`版本对比: v${compareVersion?.version || ''} vs v${selectedVersion?.version || ''}`}
        open={diffModalVisible}
        onCancel={() => setDiffModalVisible(false)}
        footer={null}
        width={900}
        loading={diffLoading}
      >
        {diffData && <VersionDiff diff={diffData} onRestore={(versionId) => {
          const version = versions.find(v => v.version === versionId);
          if (version) handleRestore(version);
        }} />}
      </Modal>
    </div>
  );
};

export default VersionList;