'use client';

import React, { useState } from 'react';
import { Tree, Input, Button, Space, MenuProps } from 'antd';
import {
  FileTextOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  PlusOutlined,
  SearchOutlined,
  MoreOutlined,
} from '@ant-design/icons';
import type { Wiki } from '@/types/wiki';
import styles from './WikiTree.module.css';

const { Search } = Input;

interface WikiTreeProps {
  wikiList: Wiki[];
  selectedId?: number;
  onSelect?: (id: number) => void;
  onCreate?: (parentId?: number) => void;
  onMove?: (id: number, parentId?: number) => void;
  onDelete?: (id: number) => void;
  loading?: boolean;
}

/**
 * Wiki 文档树组件
 * 支持展示层级结构、搜索、拖拽排序
 */
export const WikiTree: React.FC<WikiTreeProps> = ({
  wikiList,
  selectedId,
  onSelect,
  onCreate,
  onMove,
  onDelete,
  loading = false,
}) => {
  const [searchValue, setSearchValue] = useState('');
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  // 构建树形数据
  const buildTreeData = (items: Wiki[], parentId?: number): any[] => {
    return items
      .filter((item) => item.parentId === parentId)
      .sort((a, b) => a.orderNum - b.orderNum)
      .map((item) => {
        const children = buildTreeData(items, item.id);
        const isLeaf = !children.length;
        const title = (
          <div className={styles.treeNode}>
            <span className={styles.nodeTitle}>
              {isLeaf ? <FileTextOutlined /> : <FolderOutlined />}
              <span className={styles.titleText}>{item.title}</span>
            </span>
            <div className={styles.nodeActions}>
              {onCreate && (
                <Button
                  type="text"
                  size="small"
                  icon={<PlusOutlined />}
                  onClick={(e) => {
                    e.stopPropagation();
                    onCreate(item.id);
                  }}
                />
              )}
              <Dropdown
                trigger={['click']}
                menu={{
                  items: [
                    {
                      key: 'create',
                      label: '新建子文档',
                      icon: <PlusOutlined />,
                      onClick: () => onCreate?.(item.id),
                    },
                    {
                      key: 'delete',
                      label: '删除',
                      danger: true,
                      onClick: () => onDelete?.(item.id),
                    },
                  ],
                }}
              >
                <Button
                  type="text"
                  size="small"
                  icon={<MoreOutlined />}
                  onClick={(e) => e.stopPropagation()}
                />
              </Dropdown>
            </div>
          </div>
        );

        return {
          key: item.id,
          title,
          data: item,
          children: children.length > 0 ? children : undefined,
          isLeaf,
        };
      });
  };

  // 过滤搜索结果
  const filterTreeData = (data: any[], keyword: string): any[] => {
    if (!keyword) return data;

    return data
      .map((item) => {
        const matchesTitle = item.data?.title?.toLowerCase().includes(keyword.toLowerCase());
        const children = item.children ? filterTreeData(item.children, keyword) : [];

        if (matchesTitle || children.length > 0) {
          return {
            ...item,
            children,
          };
        }
        return null;
      })
      .filter(Boolean);
  };

  const treeData = buildTreeData(wikiList);
  const filteredData = filterTreeData(treeData, searchValue);

  // 生成展开的 keys
  const getAllKeys = (data: any[]): React.Key[] => {
    const keys: React.Key[] = [];
    data.forEach((item) => {
      if (item.children) {
        keys.push(item.key);
        keys.push(...getAllKeys(item.children));
      }
    });
    return keys;
  };

  const handleSearch = (value: string) => {
    setSearchValue(value);
    if (value) {
      const allKeys = getAllKeys(filteredData);
      setExpandedKeys(allKeys);
    } else {
      setExpandedKeys([]);
    }
  };

  const handleSelect = (selectedKeys: React.Key[], info: any) => {
    if (selectedKeys.length > 0 && onSelect) {
      onSelect(selectedKeys[0] as number);
    }
  };

  // 自定义拖拽行为
  const handleDrop = (info: any) => {
    const dropKey = info.node.key;
    const dragKey = info.dragNode.key;
    const dropPos = info.node.pos.split('-');
    const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1]);

    const data = [...filteredData];

    // 找到拖拽节点和目标节点
    const findNode = (nodes: any[], key: number): any => {
      for (const node of nodes) {
        if (node.key === key) return node;
        if (node.children) {
          const found = findNode(node.children, key);
          if (found) return found;
        }
      }
      return null;
    };

    const dragObj = findNode(data, dragKey);
    const dropObj = findNode(data, dropKey);

    if (!dragObj || !dropObj) return;

    // 计算新的 parentId
    let newParentId: number | undefined;
    if (info.dropToGap) {
      // 放置到同级
      const parent = findParent(data, dropKey);
      newParentId = parent?.data?.id;
    } else {
      // 放置为子级
      newParentId = dropKey;
    }

    if (onMove) {
      onMove(dragKey, newParentId);
    }
  };

  const findParent = (nodes: any[], key: number, parent: any = null): any => {
    for (const node of nodes) {
      if (node.key === key) return parent;
      if (node.children) {
        const found = findParent(node.children, key, node);
        if (found !== null) return found;
      }
    }
    return null;
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Search
          placeholder="搜索文档..."
          prefix={<SearchOutlined />}
          onChange={(e) => handleSearch(e.target.value)}
          className={styles.search}
        />
        {onCreate && (
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => onCreate(undefined)}
            className={styles.addButton}
          >
            新建
          </Button>
        )}
      </div>

      <div className={styles.treeWrapper}>
        <Tree
          treeData={filteredData}
          selectedKeys={selectedId ? [selectedId] : []}
          expandedKeys={expandedKeys}
          onSelect={handleSelect}
          onExpand={(keys) => setExpandedKeys(keys)}
          draggable
          blockNode
          showIcon={false}
          onDrop={handleDrop}
        />
      </div>
    </div>
  );
};

import { Dropdown } from 'antd';

export default WikiTree;