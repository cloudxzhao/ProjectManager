'use client';

import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Space, Select, Card, Tabs } from 'antd';
import { SaveOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons';
import type { Wiki, CreateWikiDto, UpdateWikiDto, WikiStatus } from '@/types/wiki';
import styles from './WikiEditor.module.css';

const { Title } = Typography;
const { TextArea } = Input;

interface WikiEditorProps {
  wiki?: Wiki;
  projectId: number;
  onSave: (data: CreateWikiDto | UpdateWikiDto) => Promise<void>;
  onCancel?: () => void;
  loading?: boolean;
}

/**
 * Wiki 编辑器组件
 * 支持 Markdown 编辑和预览
 */
export const WikiEditor: React.FC<WikiEditorProps> = ({
  wiki,
  projectId,
  onSave,
  onCancel,
  loading = false,
}) => {
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('write');
  const [content, setContent] = useState(wiki?.content || '');

  useEffect(() => {
    if (wiki) {
      form.setFieldsValue({
        title: wiki.title,
        content: wiki.content,
        status: wiki.status,
      });
      setContent(wiki.content || '');
    }
  }, [wiki, form]);

  // 简单的 Markdown 预览渲染
  const renderPreview = (md: string) => {
    if (!md) return '';

    let html = md;

    // 代码块
    html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
      return `<pre><code class="language-${lang}">${escapeHtml(code)}</code></pre>`;
    });

    // 行内代码
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

    // 标题
    html = html.replace(/^(#{1,6})\s+(.+)$/gm, (_, hashes, text) => {
      const level = hashes.length;
      return `<h${level}>${text}</h${level}>`;
    });

    // 粗体
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

    // 斜体
    html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');

    // 链接
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');

    // 列表
    html = html.replace(/^[-*+]\s+(.+)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>');

    // 有序列表
    html = html.replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>');

    // 引用
    html = html.replace(/^>\s+(.+)$/gm, '<blockquote>$1</blockquote>');

    // 水平线
    html = html.replace(/^---+$/gm, '<hr>');

    // 段落
    const lines = html.split('\n');
    let result = '';
    let inParagraph = false;

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed) {
        if (inParagraph) {
          result += '</p>';
          inParagraph = false;
        }
      } else if (!trimmed.startsWith('<') && !trimmed.startsWith('&')) {
        if (!inParagraph) {
          result += '<p>';
          inParagraph = true;
        } else {
          result += '<br>';
        }
        result += trimmed;
      } else {
        if (inParagraph) {
          result += '</p>';
          inParagraph = false;
        }
        result += trimmed + '\n';
      }
    }

    if (inParagraph) {
      result += '</p>';
    }

    return result;
  };

  const escapeHtml = (text: string) => {
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  };

  const handleSubmit = async (values: any) => {
    const data = wiki
      ? {
          title: values.title,
          content: values.content,
          status: values.status,
        }
      : {
          title: values.title,
          content: values.content,
          status: values.status || 'PUBLISHED',
        };
    await onSave(data);
  };

  return (
    <Card className={styles.container}>
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{
          status: wiki?.status || 'PUBLISHED',
        }}
      >
        <Form.Item
          name="title"
          label="标题"
          rules={[{ required: true, message: '请输入文档标题' }]}
        >
          <Input placeholder="请输入文档标题" maxLength={200} showCount />
        </Form.Item>

        <Form.Item name="status" label="状态">
          <Select
            options={[
              { value: 'DRAFT', label: '草稿' },
              { value: 'PUBLISHED', label: '已发布' },
              { value: 'ARCHIVED', label: '已归档' },
            ]}
          />
        </Form.Item>

        <Form.Item
          name="content"
          label="内容"
          rules={[{ required: true, message: '请输入文档内容' }]}
        >
          <div className={styles.editorContainer}>
            <Tabs
              activeKey={activeTab}
              onChange={setActiveTab}
              className={styles.tabs}
              items={[
                {
                  key: 'write',
                  label: (
                    <span>
                      <EditOutlined /> 编写
                    </span>
                  ),
                  children: (
                    <TextArea
                      value={content}
                      onChange={(e) => setContent(e.target.value)}
                      placeholder="使用 Markdown 编写文档内容..."
                      className={styles.textarea}
                      autoSize={{ minRows: 15, maxRows: 30 }}
                    />
                  ),
                },
                {
                  key: 'preview',
                  label: (
                    <span>
                      <EyeOutlined /> 预览
                    </span>
                  ),
                  children: (
                    <div
                      className={styles.preview}
                      dangerouslySetInnerHTML={{ __html: renderPreview(content) }}
                    />
                  ),
                },
              ]}
            />
          </div>
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading}>
              保存
            </Button>
            {onCancel && (
              <Button onClick={onCancel} loading={loading}>
                取消
              </Button>
            )}
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

import { Typography } from 'antd';

export default WikiEditor;