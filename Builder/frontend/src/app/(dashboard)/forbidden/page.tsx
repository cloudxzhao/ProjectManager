import { ErrorFallback } from '@/components/common/ErrorFallback';

export default function ForbiddenPage() {
  return (
    <ErrorFallback
      status="403"
      title="403"
      subTitle="抱歉，您没有权限访问此页面"
    />
  );
}
