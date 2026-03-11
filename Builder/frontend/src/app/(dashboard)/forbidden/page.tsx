'use client';

import { ErrorFallback } from '@/components/common/ErrorFallback';

interface ForbiddenPageProps {
  message?: string;
}

export default function ForbiddenPage({ message }: ForbiddenPageProps) {
  return (
    <ErrorFallback
      status="403"
      title="403"
      subTitle={message || '抱歉，您没有权限访问此页面'}
    />
  );
}
