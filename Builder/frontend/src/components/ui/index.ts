// UI 组件统一导出
// 注意：项目直接使用 Ant Design 组件，通过 className 实现统一样式

// 重新导出 Ant Design 组件
export { Button } from 'antd';
export { Input } from 'antd';
export { Card } from 'antd';
export { Badge } from 'antd';
export { Avatar } from 'antd';
export { Modal } from 'antd';
export { Table } from 'antd';
export { Dropdown } from 'antd';

// 导出统一样式类名辅助函数
export { twMerge } from 'tailwind-merge';
