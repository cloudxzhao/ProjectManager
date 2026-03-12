// UI 组件统一导出
// 项目使用 Ant Design 组件，通过 className 实现统一样式风格

// 重新导出 Ant Design 核心组件
export { Button } from 'antd';
export { Input } from 'antd';
export { Card } from 'antd';
export { Badge } from 'antd';
export { Avatar } from 'antd';
export { Modal } from 'antd';
export { Table } from 'antd';
export { Dropdown } from 'antd';
export { Divider } from 'antd';
export { Tag } from 'antd';
export { Progress } from 'antd';
export { List } from 'antd';
export { Statistic } from 'antd';
export { Checkbox } from 'antd';
export { Form } from 'antd';
export { message } from 'antd';
export { Drawer } from 'antd';

// 导出自定义 UI 组件
export {
  Button as UIButton,
  Card as UICard,
  Badge as UIBadge,
  Input as UIInput,
  Divider as UIDivider,
  Avatar as UIAvatar,
} from './Buttons';

// 导出工具函数
export { twMerge } from 'tailwind-merge';
export { cn } from '@/lib/utils/cn';
