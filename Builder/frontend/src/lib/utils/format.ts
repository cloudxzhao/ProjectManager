import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import relativeTime from 'dayjs/plugin/relativeTime';
import utc from 'dayjs/plugin/utc';

dayjs.extend(relativeTime);
dayjs.extend(utc);
dayjs.locale('zh-cn');

/**
 * 格式化日期
 */
export const formatDate = (date: string | Date, format = 'YYYY-MM-DD') => {
  return dayjs(date).format(format);
};

/**
 * 格式化相对时间
 */
export const formatRelativeTime = (date: string | Date) => {
  return dayjs(date).fromNow();
};

/**
 * 格式化日期时间
 */
export const formatDateTime = (date: string | Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
};

/**
 * 判断是否过期
 */
export const isExpired = (date: string | Date) => {
  return dayjs(date).isBefore(dayjs());
};

/**
 * 计算剩余天数
 */
export const getRemainingDays = (date: string | Date) => {
  return dayjs(date).diff(dayjs(), 'day');
};

export { dayjs };
