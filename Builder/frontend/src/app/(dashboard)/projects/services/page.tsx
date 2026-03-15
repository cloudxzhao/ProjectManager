'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  ColorPicker,
  message,
  Empty,
  Spin,
  Typography,
  Select,
  Space,
  Tag,
  InputRef,
  Dropdown,
  Popconfirm,
  Divider,
} from 'antd';
import type { TableColumnsType, MenuProps } from 'antd';
import {
  PlusOutlined,
  AppstoreOutlined,
  ArrowLeftOutlined,
  SearchOutlined,
  DownOutlined,
  RightOutlined,
  ApiOutlined,
  FilterOutlined,
  ClearOutlined,
} from '@ant-design/icons';
import {
  getEpics,
  createEpic,
  updateEpic,
  deleteEpic,
  Epic,
  CreateEpicDto,
  UpdateEpicDto,
  getServiceApis,
  createServiceApi,
  updateServiceApi,
  deleteServiceApi,
  ServiceApi,
  CreateServiceApiDto,
  UpdateServiceApiDto,
} from '@/lib/api/epic';
import { getAuthorizedProjects } from '@/lib/api/project';
import type { Project } from '@/lib/api/project';

const { TextArea } = Input;
const { Title } = Typography;

// 筛选条件类型
interface FilterState {
  search?: string;
  color?: string;
}

// 方法颜色映射
const methodColors: Record<string, string> = {
  GET: 'green',
  POST: 'blue',
  PUT: 'orange',
  DELETE: 'red',
  PATCH: 'purple',
};

export default function ServicesPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [services, setServices] = useState<Epic[]>([]);
  const [projects, setProjects] = useState<{ value: number; label: string }[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedService, setSelectedService] = useState<Epic | null>(null);
  const [form] = Form.useForm();
  const [confirmLoading, setConfirmLoading] = useState(false);

  // 筛选状态
  const [filters, setFilters] = useState<FilterState>({});
  const searchInputRef = useRef<InputRef>(null);

  // 展开的行
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);

  // 接口管理相关
  const [apiModalOpen, setApiModalOpen] = useState(false);
  const [apiForm] = Form.useForm();
  const [currentEpicId, setCurrentEpicId] = useState<number | null>(null);
  const [currentEpicTitle, setCurrentEpicTitle] = useState<string>('');
  const [serviceApis, setServiceApis] = useState<ServiceApi[]>([]);
  const [apisLoading, setApisLoading] = useState(false);
  const [selectedApi, setSelectedApi] = useState<ServiceApi | null>(null);
  const [apiDeleteModalOpen, setApiDeleteModalOpen] = useState(false);

  // 获取有权限的项目列表
  const fetchProjects = async () => {
    try {
      const res = await getAuthorizedProjects();
      const projectList = res.list || [];
      setProjects(projectList.map((p: Project) => ({ value: p.id, label: p.name })));
      if (projectList.length > 0 && !selectedProjectId) {
        setSelectedProjectId(projectList[0].id);
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取项目列表失败';
      message.error(errorMessage);
    }
  };

  // 获取服务列表
  const fetchServices = async (projectId: number) => {
    setLoading(true);
    try {
      const data = await getEpics(projectId);
      setServices(data);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取服务列表失败';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 获取服务下的接口列表
  const fetchServiceApis = async (epicId: number, title: string) => {
    if (!selectedProjectId) return;
    setApisLoading(true);
    setCurrentEpicId(epicId);
    setCurrentEpicTitle(title);
    try {
      const data = await getServiceApis(selectedProjectId, epicId);
      setServiceApis(data);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '获取接口列表失败';
      message.error(errorMessage);
    } finally {
      setApisLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    if (selectedProjectId) {
      fetchServices(selectedProjectId);
    }
  }, [selectedProjectId]);

  // 筛选逻辑
  const filteredServices = services.filter((service) => {
    if (filters.search && !service.title.toLowerCase().includes(filters.search.toLowerCase())) {
      return false;
    }
    if (filters.color && service.color !== filters.color) {
      return false;
    }
    return true;
  });

  // 清除筛选
  const clearFilters = () => {
    setFilters({});
    if (searchInputRef.current) {
      searchInputRef.current.blur();
    }
  };

  // 展开/收起行
  const handleExpand = async (expanded: boolean, record: Epic) => {
    if (expanded) {
      await fetchServiceApis(record.id, record.title);
      setExpandedRowKeys([record.id]);
    } else {
      setExpandedRowKeys([]);
      setServiceApis([]);
    }
  };

  // 打开创建弹框
  const handleCreate = () => {
    if (!selectedProjectId) {
      message.error('请先选择项目');
      return;
    }
    setSelectedService(null);
    form.resetFields();
    setModalOpen(true);
  };

  // 打开编辑弹框
  const handleEdit = (record: Epic) => {
    setSelectedService(record);
    form.setFieldsValue({
      title: record.title,
      description: record.description,
      color: record.color,
    });
    setModalOpen(true);
  };

  // 打开删除确认弹框
  const handleDelete = (record: Epic) => {
    setSelectedService(record);
    setDeleteModalOpen(true);
  };

  // 打开接口管理弹框
  const handleManageApis = async (record: Epic) => {
    await fetchServiceApis(record.id, record.title);
    setApiModalOpen(true);
  };

  // 打开创建接口弹框
  const handleCreateApi = () => {
    setSelectedApi(null);
    apiForm.resetFields();
  };

  // 打开编辑接口弹框
  const handleEditApi = (api: ServiceApi) => {
    setSelectedApi(api);
    apiForm.setFieldsValue({
      name: api.name,
      path: api.path,
      method: api.method,
      description: api.description,
      status: api.status,
    });
  };

  // 提交接口创建/编辑
  const handleApiSubmit = async () => {
    if (!selectedProjectId || !currentEpicId) return;

    try {
      await apiForm.validateFields();
      const values = apiForm.getFieldsValue();
      setConfirmLoading(true);

      if (selectedApi) {
        // 编辑
        await updateServiceApi(selectedProjectId, currentEpicId, selectedApi.id, {
          name: values.name,
          path: values.path,
          method: values.method,
          description: values.description,
          status: values.status,
        });
        message.success('接口更新成功');
      } else {
        // 创建
        await createServiceApi(selectedProjectId, currentEpicId, {
          name: values.name,
          path: values.path,
          method: values.method,
          description: values.description,
        });
        message.success('接口创建成功');
      }

      setApiModalOpen(false);
      apiForm.resetFields();
      fetchServiceApis(currentEpicId, currentEpicTitle);
    } catch (error: unknown) {
      if (error instanceof Error && error.message !== 'Validation failed') {
        const errorMessage = error.message || '操作失败，请稍后重试';
        message.error(errorMessage);
      }
    } finally {
      setConfirmLoading(false);
    }
  };

  // 删除接口
  const handleDeleteApi = async (apiId: number) => {
    if (!selectedProjectId || !currentEpicId) return;

    try {
      await deleteServiceApi(selectedProjectId, currentEpicId, apiId);
      message.success('接口删除成功');
      setApiDeleteModalOpen(false);
      fetchServiceApis(currentEpicId, currentEpicTitle);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '删除失败，请稍后重试';
      message.error(errorMessage);
    }
  };

  // 提交创建/编辑
  const handleSubmit = async () => {
    if (!selectedProjectId) {
      message.error('请先选择项目');
      return;
    }

    try {
      await form.validateFields();
      const values = form.getFieldsValue();
      setConfirmLoading(true);

      if (selectedService) {
        // 编辑
        await updateEpic(selectedProjectId, selectedService.id, {
          title: values.title,
          description: values.description,
          color: values.color,
        });
        message.success('服务更新成功');
      } else {
        // 创建
        await createEpic(selectedProjectId, {
          title: values.title,
          description: values.description,
          color: values.color || '#1890ff',
        });
        message.success('服务创建成功');
      }

      setModalOpen(false);
      form.resetFields();
      fetchServices(selectedProjectId);
    } catch (error: unknown) {
      if (error instanceof Error && error.message !== 'Validation failed') {
        const errorMessage = error.message || '操作失败，请稍后重试';
        message.error(errorMessage);
      }
    } finally {
      setConfirmLoading(false);
    }
  };

  // 确认删除
  const handleDeleteConfirm = async () => {
    if (!selectedService || !selectedProjectId) return;

    setConfirmLoading(true);
    try {
      await deleteEpic(selectedProjectId, selectedService.id);
      message.success('服务删除成功');
      setDeleteModalOpen(false);
      fetchServices(selectedProjectId);
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '删除失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setConfirmLoading(false);
    }
  };

  // 接口列表 columns
  const apiColumns: TableColumnsType<ServiceApi> = [
    {
      title: '方法',
      dataIndex: 'method',
      key: 'method',
      width: 100,
      render: (method: string) => (
        <Tag color={methodColors[method] || 'default'}>{method}</Tag>
      ),
    },
    {
      title: '接口名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: '路径',
      dataIndex: 'path',
      key: 'path',
      ellipsis: true,
      render: (path: string) => <code className="text-xs bg-gray-800 px-2 py-1 rounded">{path}</code>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 200,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          active: { color: 'green', text: '启用' },
          deprecated: { color: 'orange', text: '废弃' },
          draft: { color: 'gray', text: '草稿' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: unknown, record: ServiceApi) => (
        <Space size="small">
          <button
            onClick={() => handleEditApi(record)}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
            style={{ color: '#636e72' }}
            title="编辑"
          >
            ✏️
          </button>
          <Popconfirm
            title="确认删除"
            description={`确定要删除接口 "${record.name}" 吗？`}
            onConfirm={() => handleDeleteApi(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <button
              className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
              style={{ color: '#636e72' }}
              title="删除"
            >
              🗑️
            </button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const columns: TableColumnsType<Epic> = [
    {
      title: '',
      key: 'expand',
      width: 50,
      render: (_: unknown, record: Epic) => {
        const expanded = expandedRowKeys.includes(record.id);
        return (
          <Button
            type="text"
            size="small"
            icon={expanded ? <DownOutlined /> : <RightOutlined />}
            onClick={(e) => {
              e.stopPropagation();
              handleExpand(!expanded, record);
            }}
          />
        );
      },
    },
    {
      title: '服务名称',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      render: (title: string, record: Epic) => (
        <div className="flex items-center gap-2">
          <div
            className="w-3 h-3 rounded-full"
            style={{ backgroundColor: record.color || '#1890ff' }}
          />
          <span className="font-medium text-white">{title}</span>
        </div>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300,
      render: (description?: string) => (
        <span className="text-gray-400 text-sm" title={description}>
          {description && description.length > 50 ? `${description.slice(0, 50)}...` : description || '-'}
        </span>
      ),
    },
    {
      title: '位置',
      dataIndex: 'position',
      key: 'position',
      width: 80,
      render: (position?: number) => (
        <span className="text-gray-400">{position !== undefined ? `#${position}` : '-'}</span>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => (
        <span className="text-gray-400">{new Date(createdAt).toLocaleString('zh-CN')}</span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_: unknown, record: Epic) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<ApiOutlined />}
            onClick={() => handleManageApis(record)}
          >
            接口
          </Button>
          <button
            onClick={() => handleEdit(record)}
            className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
            style={{ color: '#636e72' }}
            title="编辑"
          >
            ✏️
          </button>
          <Popconfirm
            title="确认删除"
            description={`确定要删除服务 "${record.title}" 吗？`}
            onConfirm={() => handleDelete(record)}
            okText="确定"
            cancelText="取消"
          >
            <button
              className="text-lg cursor-pointer transition-colors duration-200 hover:scale-110 bg-transparent border-none p-0"
              style={{ color: '#636e72' }}
              title="删除"
            >
              🗑️
            </button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* 头部操作区 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => router.push('/projects')}
            className="text-gray-400 hover:text-white"
          />
          <div className="flex items-center gap-2">
            <AppstoreOutlined className="text-orange-500" style={{ fontSize: 20 }} />
            <Title level={4} className="text-white mb-0">服务管理</Title>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <Select
            placeholder="选择项目"
            value={selectedProjectId}
            onChange={setSelectedProjectId}
            className="w-48"
            options={projects}
          />
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
            disabled={!selectedProjectId}
          >
            新建服务
          </Button>
        </div>
      </div>

      {/* 筛选区 */}
      <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FilterOutlined className="text-gray-400" />
            <span className="text-gray-300 font-medium">筛选</span>
          </div>
          {(filters.search || filters.color) && (
            <Button
              type="link"
              size="small"
              icon={<ClearOutlined />}
              onClick={clearFilters}
              className="text-gray-400 hover:text-white"
            >
              清除筛选
            </Button>
          )}
        </div>
        <Divider className="border-gray-700 my-3" />
        <Space size="large">
          <div className="flex items-center gap-2">
            <span className="text-gray-400 text-sm">搜索:</span>
            <Input
              ref={searchInputRef}
              placeholder="搜索服务名称"
              value={filters.search || ''}
              onChange={(e) => setFilters({ ...filters, search: e.target.value })}
              allowClear
              className="w-64"
              prefix={<SearchOutlined className="text-gray-400" />}
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-gray-400 text-sm">颜色:</span>
            <Select
              placeholder="全部颜色"
              value={filters.color || undefined}
              onChange={(value) => setFilters({ ...filters, color: value })}
              allowClear
              className="w-40"
              options={[
                { value: '#1890ff', label: '蓝色' },
                { value: '#52c41a', label: '绿色' },
                { value: '#faad14', label: '橙色' },
                { value: '#eb2f96', label: '粉色' },
                { value: '#722ed1', label: '紫色' },
                { value: '#13c2c2', label: '青色' },
                { value: '#f5222d', label: '红色' },
              ]}
            />
          </div>
        </Space>
      </div>

      {/* 服务列表表格 */}
      {loading ? (
        <div className="text-center py-12">
          <Spin size="large" description="加载服务列表中..." />
        </div>
      ) : !selectedProjectId ? (
        <Empty
          description="请先选择项目"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      ) : filteredServices.length > 0 ? (
        <Table<Epic>
          dataSource={filteredServices}
          rowKey="id"
          columns={columns}
          pagination={false}
          className="services-table"
          expandedRowKeys={expandedRowKeys}
          onExpand={handleExpand}
          expandable={{
            expandedRowRender: (record) => (
              <div className="bg-gray-800/30 rounded-lg p-4 ml-8">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-2">
                    <ApiOutlined className="text-blue-400" />
                    <span className="text-gray-300 font-medium">
                      {record.title} - 接口列表
                    </span>
                  </div>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => handleManageApis(record)}
                  >
                    添加接口
                  </Button>
                </div>
                {apisLoading ? (
                  <div className="text-center py-8">
                    <Spin size="small" />
                  </div>
                ) : serviceApis.length > 0 ? (
                  <Table<ServiceApi>
                    dataSource={serviceApis}
                    rowKey="id"
                    columns={apiColumns}
                    pagination={false}
                    size="small"
                    showHeader={false}
                  />
                ) : (
                  <Empty
                    description="暂无接口"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                )}
              </div>
            ),
          }}
        />
      ) : (
        <Empty
          description="暂无服务"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        >
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
            disabled={!selectedProjectId}
          >
            创建第一个服务
          </Button>
        </Empty>
      )}

      {/* 创建/编辑弹框 */}
      <Modal
        title={selectedService ? '编辑服务' : '新建服务'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={confirmLoading}
        width={600}
      >
        <Form form={form} layout="vertical" className="mt-4">
          <Form.Item
            name="title"
            label="服务名称"
            rules={[{ required: true, message: '请输入服务名称' }]}
          >
            <Input placeholder="请输入服务名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="服务描述"
            rules={[{ required: false }]}
          >
            <TextArea
              rows={4}
              placeholder="请输入服务描述（可选）"
            />
          </Form.Item>
          <Form.Item
            name="color"
            label="服务颜色"
            initialValue="#1890ff"
          >
            <ColorPicker showText format="hex" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 删除确认弹框 */}
      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        confirmLoading={confirmLoading}
      >
        <p>
          确定要删除服务 <span className="font-medium text-white">{selectedService?.title}</span> 吗？
          此操作不可恢复。
        </p>
      </Modal>

      {/* 接口管理弹框 */}
      <Modal
        title={
          <div className="flex items-center gap-2">
            <ApiOutlined className="text-blue-400" />
            <span>{currentEpicTitle} - 接口管理</span>
          </div>
        }
        open={apiModalOpen}
        onCancel={() => setApiModalOpen(false)}
        footer={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateApi}>
            添加接口
          </Button>
        }
        width={900}
      >
        {apisLoading ? (
          <div className="text-center py-12">
            <Spin size="large" />
          </div>
        ) : serviceApis.length > 0 ? (
          <Table<ServiceApi>
            dataSource={serviceApis}
            rowKey="id"
            columns={apiColumns}
            pagination={false}
            scroll={{ y: 400 }}
          />
        ) : (
          <Empty description="暂无接口">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreateApi}
            >
              添加第一个接口
            </Button>
          </Empty>
        )}
      </Modal>

      {/* 接口创建/编辑弹框 */}
      <Modal
        title={selectedApi ? '编辑接口' : '新建接口'}
        open={apiModalOpen && !!apiForm.getFieldValue('name') && !serviceApis.length}
        onOk={handleApiSubmit}
        onCancel={() => {
          apiForm.resetFields();
          setApiModalOpen(false);
        }}
        confirmLoading={confirmLoading}
        width={600}
      >
        <Form form={apiForm} layout="vertical" className="mt-4">
          <Form.Item
            name="name"
            label="接口名称"
            rules={[{ required: true, message: '请输入接口名称' }]}
          >
            <Input placeholder="请输入接口名称" />
          </Form.Item>
          <Form.Item
            name="path"
            label="接口路径"
            rules={[{ required: true, message: '请输入接口路径' }]}
          >
            <Input placeholder="例如：/api/v1/users" />
          </Form.Item>
          <Form.Item
            name="method"
            label="请求方法"
            rules={[{ required: true, message: '请选择请求方法' }]}
          >
            <Select>
              <Select.Option value="GET">GET</Select.Option>
              <Select.Option value="POST">POST</Select.Option>
              <Select.Option value="PUT">PUT</Select.Option>
              <Select.Option value="DELETE">DELETE</Select.Option>
              <Select.Option value="PATCH">PATCH</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="description"
            label="接口描述"
            rules={[{ required: false }]}
          >
            <TextArea rows={3} placeholder="请输入接口描述（可选）" />
          </Form.Item>
          {selectedApi && (
            <Form.Item
              name="status"
              label="接口状态"
              rules={[{ required: true, message: '请选择接口状态' }]}
            >
              <Select>
                <Select.Option value="active">启用</Select.Option>
                <Select.Option value="deprecated">废弃</Select.Option>
                <Select.Option value="draft">草稿</Select.Option>
              </Select>
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
}
