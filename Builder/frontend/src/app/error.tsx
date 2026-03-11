'use client';

import { ErrorFallback } from '@/components/common/ErrorFallback';

interface GlobalErrorProps {
  error: Error;
  reset: () => void;
}

export default function GlobalError({ error, reset }: GlobalErrorProps) {
  return (
    <html>
      <body>
        <ErrorFallback
          status="500"
          error={error}
          reset={reset}
          title="500"
          subTitle="服务器开小差了，请稍后再试"
        />
      </body>
    </html>
  );
}
