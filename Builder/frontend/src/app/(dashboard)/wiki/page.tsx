'use client';

import { useState, useEffect } from 'react';
import { Card, Input, Select, Button, Avatar, Tag, Empty, Spin, Pagination, Modal, Form, Drawer, message, Typography, Space, Divider } from 'antd';
import { BookOutlined, PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined, FolderOutlined, FileTextOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getWikis, getWiki, deleteWiki, createWiki, updateWiki, type Wiki } from '@/lib/api/wiki';
import { getAuthorizedProjects } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';
import { WikiStatus } from '@/types/wiki';

const { Option } = Select;
const { TextArea } = Input;
const { Title, Text, Paragraph } = Typography;

// 状态颜色映射
const statusColorMap: Record<string, string> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  ARCHIVED: 'default',
};

// 状态文本映射
const statusTextMap: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  ARCHIVED: '已归档',
};

interface WikiCardProps {
  wiki: Wiki;
  project?: Project;
  onView: (wiki: Wiki) => void;
  onEdit: (wiki: Wiki) => void;
  onDelete: (wiki: Wiki) => void;
}

// Wiki 文档卡片组件
const WikiCard: React.FC<WikiCardProps> = ({ wiki, project, onView, onEdit, onDelete }) => {
  return (
    <Card
      hoverable
      className="h-full transition-all duration-300 group"
      style={{
        background: 'linear-gradient(145deg, #1e2230, #161922)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '16px',
        padding: '0',
        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.5)',
      }}
      bodyStyle={{ padding: 0 }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-5px)';
        e.currentTarget.style.boxShadow = '0 15px 40px rgba(0, 0, 0, 0.6)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.15)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.5)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
      }}
    >
      <div className="p-5">
        {/* 头部：图标 + 状态 */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-xl flex items-center justify-center text-xl"
              style={{
                background: `linear-gradient(135deg, ${project?.color || '#8b5cf6'} 0%, ${project?.color || '#8b5cf6'}cc 100%)`,
                borderRadius: '12px',
                boxShadow: `0 4px 12px ${project?.color || '#8b5cf6'}40`,
              }}
            >
              {wiki.parentId ? <FileTextOutlined style={{ color: '#fff' }} /> : <FolderOutlined style={{ color: '#fff' }} />}
            </div>
            <Tag
              color={statusColorMap[wiki.status] as any}
              className="text-xs font-bold uppercase tracking-wider"
            >
              {statusTextMap[wiki.status]}
            </Tag>
          </div>
          <Tag color="blue" className="text-xs">v{wiki.version}</Tag>
        </div>

        {/* 标题 */}
        <h3
          className="text-base font-semibold mb-2 line-clamp-2"
          style={{
            color: '#ffffff',
            letterSpacing: '0.5px',
          }}
        >
          {wiki.title}
        </h3>

        {/* 描述/摘要 */}
        {wiki.summary && (
          <p
            className="text-sm mb-4 line-clamp-2"
            style={{
              color: '#808695',
              lineHeight: 1.6,
            }}
          >
            {wiki.summary}
          </p>
        )}

        {/* 分割线 */}
        <div
          className="mb-4"
          style={{
            height: '1px',
            background: 'rgba(255, 255, 255, 0.08)',
          }}
        />

        {/* 底部信息 */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Avatar
              size={24}
              style={{
                background: 'linear-gradient(135deg, #3a8dff, #0052d4)',
              }}
              icon={<span className="text-xs">{wiki.authorName?.charAt(0).toUpperCase() || 'U'}</span>}
            />
            <Text className="text-xs" style={{ color: '#808695' }}>
              {wiki.authorName || '未知'}
            </Text>
          </div>
          <div className="flex items-center gap-1 text-xs" style={{ color: '#808695' }}>
            <EyeOutlined /> {wiki.viewCount}
          </div>
        </div>
      </div>

      {/* 底部项目信息 */}
      {project && (
        <div
          className="px-5 py-3 border-t flex items-center justify-between"
          style={{
            background: 'rgba(0,0,0,0.2)',
            borderColor: 'rgba(255, 255, 255, 0.08)',
          }}
        >
          <div className="flex items-center gap-2" onClick={() => onView(wiki)} style={{ cursor: 'pointer' }}>
            <div
              className="w-5 h-5 rounded flex items-center justify-center text-xs"
              style={{
                background: `linear-gradient(135deg, ${project.color || '#f97316'} 0%, ${project.color || '#f97316'}cc 100%)`,
              }}
            >
              {project.icon || '📁'}
            </div>
            <span className="text-xs font-medium" style={{ color: '#a0a6b5' }}>
              {project.name}
            </span>
          </div>
          <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => onEdit(wiki)}
              style={{ color: '#3a8dff', padding: '4px 8px' }}
            />
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={() => onDelete(wiki)}
              style={{ color: '#f85149', padding: '4px 8px' }}
            />
          </div>
        </div>
      )}
    </Card>
  );
};

// 主页面组件
export default function WikiPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [wikiList, setWikiList] = useState<Wiki[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProject, setSelectedProject] = useState<number | undefined>();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(12);
  const [total, setTotal] = useState(0);

  // 详情抽屉
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedWiki, setSelectedWiki] = useState<Wiki | null>(null);

  // 新建/编辑抽屉
  const [formOpen, setFormOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editingWiki, setEditingWiki] = useState<Wiki | null>(null);
  const [form] = Form.useForm();

  // 加载项目列表
  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const res = await getAuthorizedProjects();
        setProjects(res.list || []);
      } catch (error) {
        console.error('加载项目列表失败:', error);
      }
    };
    fetchProjects();
  }, []);

  // 加载 Wiki 列表
  useEffect(() => {
    const fetchWikis = async () => {
      setLoading(true);
      try {
        // 获取所有项目的 Wiki
        const allWikis: Wiki[] = [];

        if (selectedProject) {
          // 如果选择了项目，只获取该项目的 Wiki
          const wikis = await getWikis(selectedProject);
          allWikis.push(...wikis);
        } else {
          // 否则获取所有项目的 Wiki
          for (const project of projects) {
            try {
              const wikis = await getWikis(project.id);
              allWikis.push(...wikis.map((wiki: Wiki) => ({ ...wiki, projectName: project.name })));
            } catch (error) {
              console.error(`加载项目 ${project.name} 的 Wiki 失败:`, error);
            }
          }
        }

        // 过滤
        let filtered = allWikis;

        if (searchKeyword) {
          filtered = filtered.filter(wiki =>
            wiki.title.toLowerCase().includes(searchKeyword.toLowerCase())
          );
        }

        if (statusFilter) {
          filtered = filtered.filter(wiki => wiki.status === statusFilter);
        }

        setTotal(filtered.length);
        setWikiList(filtered);
      } catch (error) {
        console.error('加载 Wiki 列表失败:', error);
        message.error('加载 Wiki 列表失败');
      } finally {
        setLoading(false);
      }
    };

    fetchWikis();
  }, [selectedProject, searchKeyword, statusFilter, projects]);

  // 处理查看
  const handleView = (wiki: Wiki) => {
    setSelectedWiki(wiki);
    setDetailOpen(true);
  };

  // 处理新建
  const handleCreate = () => {
    setEditingWiki(null);
    form.resetFields();
    // 如果已选择项目，默认选中
    if (selectedProject) {
      form.setFieldsValue({ projectId: selectedProject });
    }
    setFormOpen(true);
  };

  // 处理编辑
  const handleEdit = (wiki: Wiki) => {
    setEditingWiki(wiki);
    form.setFieldsValue({
      title: wiki.title,
      summary: wiki.summary,
      content: wiki.content,
      status: wiki.status,
    });
    setFormOpen(true);
  };

  // 处理表单提交
  const handleFormSubmit = async (values: any) => {
    setFormLoading(true);
    try {
      if (editingWiki) {
        // 编辑模式
        await updateWiki(editingWiki.projectId, editingWiki.id, {
          title: values.title,
          summary: values.summary,
          content: values.content,
          status: values.status,
          changeLog: values.changeLog,
        });
        message.success('文档更新成功');
      } else {
        // 新建模式
        if (!values.projectId) {
          message.error('请选择项目');
          return;
        }
        await createWiki(values.projectId, {
          title: values.title,
          summary: values.summary,
          content: values.content,
          status: values.status,
        });
        message.success('文档创建成功');
      }
      setFormOpen(false);
      form.resetFields();
      // 刷新列表 - 重新触发 useEffect
      setSelectedProject(selectedProject); // 触发 useEffect
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '操作失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setFormLoading(false);
    }
  };

  // 处理删除
  const handleDelete = (wiki: Wiki) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除文档"${wiki.title}"吗？此操作不可恢复。`,
      okText: '确认删除',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await deleteWiki(wiki.projectId, wiki.id);
          message.success('删除成功');
          setWikiList(prev => prev.filter(w => w.id !== wiki.id));
          setTotal(prev => prev - 1);
        } catch (error) {
          console.error('删除失败:', error);
          message.error('删除失败');
        }
      },
    });
  };

  // 跳转到项目 Wiki
  const goToProjectWiki = (projectId: number) => {
    router.push(`/projects/${projectId}?tab=wiki`);
  };

  // 按项目分组
  const wikiByProject = wikiList.reduce((acc, wiki) => {
    const projectId = wiki.projectId;
    if (!acc[projectId]) {
      acc[projectId] = [];
    }
    acc[projectId].push(wiki);
    return acc;
  }, {} as Record<number, Wiki[]>);

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <Title level={2} style={{ margin: 0, color: '#fff' }}>
            <BookOutlined className="mr-2" />
            知识库
          </Title>
          <Text type="secondary" className="mt-1">
            查看和管理所有项目的 Wiki 文档
          </Text>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreate}
          style={{
            background: '#ff8c42',
            border: 'none',
            borderRadius: '6px',
            padding: '10px 20px',
            fontWeight: 'bold',
          }}
        >
          新建文档
        </Button>
      </div>

      {/* 筛选栏 */}
      <Card
        className="glass-dark"
        style={{
          background: 'rgba(30, 34, 48, 0.5)',
          border: '1px solid rgba(255, 255, 255, 0.08)',
        }}
      >
        <div className="flex flex-wrap items-center gap-3">
          <Input
            placeholder="搜索文档标题..."
            prefix={<SearchOutlined />}
            style={{ width: 240 }}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            allowClear
          />
          <Select
            placeholder="项目筛选"
            style={{ width: 200 }}
            value={selectedProject}
            onChange={setSelectedProject}
            allowClear
            options={projects.map(p => ({ label: p.name, value: p.id }))}
          />
          <Select
            placeholder="状态筛选"
            style={{ width: 120 }}
            value={statusFilter}
            onChange={setStatusFilter}
            allowClear
            options={Object.entries(statusTextMap).map(([key, value]) => ({
              label: value,
              value: key,
            }))}
          />
          <div className="flex-1" />
          <Text type="secondary" className="text-sm">
            共 {total} 篇文档
          </Text>
        </div>
      </Card>

      {/* Wiki 文档列表 */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin size="large" description="加载 Wiki 文档中..." />
        </div>
      ) : wikiList.length > 0 ? (
        <>
          {/* 按项目分组展示 */}
          {Object.entries(wikiByProject).map(([projectId, wikis]) => {
            const project = projects.find(p => p.id === Number(projectId));
            return (
              <div key={projectId} className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div
                      className="w-6 h-6 rounded flex items-center justify-center text-sm"
                      style={{
                        background: `linear-gradient(135deg, ${project?.color || '#f97316'} 0%, ${project?.color || '#f97316'}cc 100%)`,
                      }}
                    >
                      {project?.icon || '📁'}
                    </div>
                    <Title level={5} style={{ margin: 0, color: '#fff' }}>
                      {project?.name || `项目 ${projectId}`}
                    </Title>
                  </div>
                  <Button
                    type="link"
                    onClick={() => goToProjectWiki(Number(projectId))}
                    style={{ color: '#3a8dff' }}
                  >
                    查看全部
                  </Button>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                  {wikis.map((wiki) => (
                    <WikiCard
                      key={wiki.id}
                      wiki={wiki}
                      project={project}
                      onView={handleView}
                      onEdit={handleEdit}
                      onDelete={handleDelete}
                    />
                  ))}
                </div>
                <Divider style={{ borderColor: 'rgba(255, 255, 255, 0.08)' }} />
              </div>
            );
          })}
        </>
      ) : (
        <div className="flex flex-col items-center justify-center py-20">
          <BookOutlined style={{ fontSize: 64, color: '#4b5563' }} />
          <Title level={5} style={{ color: '#6b7280', marginTop: 16 }}>
            暂无 Wiki 文档
          </Title>
          <Text type="secondary">
            {searchKeyword || statusFilter ? '尝试调整筛选条件' : '在项目中创建第一篇 Wiki 文档吧'}
          </Text>
        </div>
      )}

      {/* 详情抽屉 */}
      <Drawer
        title={selectedWiki?.title}
        placement="right"
        width={720}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        extra={
          <Space>
            <Button icon={<EditOutlined />} onClick={() => selectedWiki && handleEdit(selectedWiki)}>
              编辑
            </Button>
            <Button danger icon={<DeleteOutlined />} onClick={() => {
              if (selectedWiki) handleDelete(selectedWiki);
              setDetailOpen(false);
            }}>
              删除
            </Button>
          </Space>
        }
      >
        {selectedWiki && (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Tag color={statusColorMap[selectedWiki.status]}>
                {statusTextMap[selectedWiki.status]}
              </Tag>
              <Tag>v{selectedWiki.version}</Tag>
            </div>

            <div>
              <Text strong>作者：</Text>
              <Text>{selectedWiki.authorName || '未知'}</Text>
            </div>

            <div>
              <Text strong>创建时间：</Text>
              <Text>{new Date(selectedWiki.createdAt).toLocaleString('zh-CN')}</Text>
            </div>

            <div>
              <Text strong>浏览次数：</Text>
              <Text>{selectedWiki.viewCount}</Text>
            </div>

            {selectedWiki.summary && (
              <div>
                <Text strong>摘要：</Text>
                <Paragraph type="secondary" className="mt-1">
                  {selectedWiki.summary}
                </Paragraph>
              </div>
            )}

            <Divider />

            <div>
              <Title level={5}>内容</Title>
              <div
                className="prose prose-invert max-w-none"
                dangerouslySetInnerHTML={{
                  __html: selectedWiki.contentHtml || selectedWiki.content
                }}
              />
            </div>
          </div>
        )}
      </Drawer>

      {/* 新建/编辑表单抽屉 */}
      <Drawer
        title={editingWiki ? '编辑 Wiki 文档' : '新建 Wiki 文档'}
        placement="right"
        width={720}
        open={formOpen}
        onClose={() => setFormOpen(false)}
        styles={{
          header: {
            background: '#161b22',
            borderBottom: '1px solid rgba(255,255,255,0.05)',
            padding: '20px 24px',
          },
          body: {
            background: '#161b22',
            color: '#f0f6fc',
            padding: '24px',
          },
          footer: {
            background: '#161b22',
            borderTop: '1px solid rgba(255,255,255,0.05)',
          },
        }}
        footer={
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button
              onClick={() => setFormOpen(false)}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                color: '#c9d1d9',
                borderRadius: '6px',
                padding: '8px 16px',
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={() => form.submit()}
              loading={formLoading}
              style={{
                background: '#ff8c42',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 24px',
                fontWeight: 'bold',
              }}
            >
              {editingWiki ? '保存更改' : '创建文档'}
            </Button>
          </div>
        }
      >
        <div style={{ height: '100%', overflowY: 'auto' }}>
          <Form
            form={form}
            layout="vertical"
            onFinish={handleFormSubmit}
            size="large"
            initialValues={{
              status: 'PUBLISHED',
            }}
          >
            {!editingWiki && (
              <Form.Item
                name="projectId"
                label="选择项目"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
                rules={[{ required: true, message: '请选择项目' }]}
              >
                <Select
                  placeholder="请选择项目"
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                    padding: '0 10px',
                  }}
                  options={projects.map(p => ({
                    label: p.name,
                    value: p.id,
                  }))}
                  dropdownStyle={{
                    background: '#161b22',
                    color: '#f0f6fc',
                  }}
                />
              </Form.Item>
            )}

            <Form.Item
              name="title"
              label="标题"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入 Wiki 标题' }]}
            >
              <Input
                placeholder="请输入标题"
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="summary"
              label="摘要"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <TextArea
                rows={3}
                placeholder="输入文档摘要（可选）..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="content"
              label="内容"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              rules={[{ required: true, message: '请输入 Wiki 内容' }]}
            >
              <TextArea
                rows={12}
                placeholder="输入 Wiki 内容（支持 Markdown）..."
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                  padding: '10px',
                  minHeight: '250px',
                }}
              />
            </Form.Item>

            <Form.Item
              name="status"
              label="状态"
              labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
            >
              <Select
                style={{
                  background: 'rgba(255,255,255,0.05)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: '#f0f6fc',
                  borderRadius: '6px',
                }}
                dropdownStyle={{
                  background: '#161b22',
                  color: '#f0f6fc',
                }}
              >
                <Select.Option value="DRAFT">草稿</Select.Option>
                <Select.Option value="PUBLISHED">已发布</Select.Option>
                <Select.Option value="ARCHIVED">已归档</Select.Option>
              </Select>
            </Form.Item>

            {editingWiki && (
              <Form.Item
                name="changeLog"
                label="变更日志"
                labelCol={{ style: { color: '#8b949e', fontSize: '13px' } }}
              >
                <TextArea
                  rows={2}
                  placeholder="简要描述本次更改的内容（可选）"
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: '#f0f6fc',
                    borderRadius: '6px',
                    padding: '10px',
                  }}
                />
              </Form.Item>
            )}
          </Form>
        </div>
      </Drawer>
    </div>
  );
}
