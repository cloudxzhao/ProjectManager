import { ErrorFallback } from '@/components/common/ErrorFallback';

export default function NotFound() {
  return (
    <ErrorFallback
      status="404"
      title="404"
      subTitle="抱歉，您访问的页面不存在"
    />
  );
}
