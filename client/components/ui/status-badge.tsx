'use client';

import { cn } from '@/lib/utils';
import type { QuoteStatus, InvoiceStatus } from '@/lib/types';

type Status = QuoteStatus | InvoiceStatus;

interface StatusBadgeProps {
  status: Status;
  className?: string;
}

const statusConfig: Record<Status, { label: string; className: string }> = {
  draft: {
    label: 'Draft',
    className: 'bg-muted text-muted-foreground',
  },
  sent: {
    label: 'Sent',
    className: 'bg-primary/10 text-primary',
  },
  accepted: {
    label: 'Accepted',
    className: 'bg-success/10 text-success',
  },
  rejected: {
    label: 'Rejected',
    className: 'bg-destructive/10 text-destructive',
  },
  expired: {
    label: 'Expired',
    className: 'bg-muted text-muted-foreground',
  },
  converted: {
    label: 'Converted',
    className: 'bg-primary/10 text-primary',
  },
  archived: {
    label: 'Archived',
    className: 'bg-muted text-muted-foreground',
  },
  issued: {
    label: 'Issued',
    className: 'bg-primary/10 text-primary',
  },
  paid: {
    label: 'Paid',
    className: 'bg-success/10 text-success',
  },
  overdue: {
    label: 'Overdue',
    className: 'bg-destructive/10 text-destructive',
  },
  cancelled: {
    label: 'Cancelled',
    className: 'bg-muted text-muted-foreground',
  },
};

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const config = statusConfig[status];

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        config.className,
        className
      )}
    >
      {config.label}
    </span>
  );
}
