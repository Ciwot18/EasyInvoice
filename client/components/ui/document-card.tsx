'use client';

import { Link } from 'react-router-dom';
import { MoreVertical, Calendar, User } from 'lucide-react';
import { cn, formatCurrency, formatDate } from '@/lib/utils';
import { StatusBadge } from './status-badge';
import { Button } from './button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from './dropdown-menu';
import type { Quote, Invoice, Client } from '@/lib/types';

interface DocumentCardProps {
  document: Quote | Invoice;
  client: Client | undefined;
  type: 'quote' | 'invoice';
  onEdit?: () => void;
  onDelete?: () => void;
  onDuplicate?: () => void;
  onConvert?: () => void;
  className?: string;
}

export function DocumentCard({
  document,
  client,
  type,
  onEdit,
  onDelete,
  onDuplicate,
  onConvert,
  className,
}: DocumentCardProps) {
  const isQuote = type === 'quote';
  const quote = isQuote ? (document as Quote) : null;
  const invoice = !isQuote ? (document as Invoice) : null;

  const total = isQuote
    ? quote?.versions[quote.currentVersion - 1]?.total || 0
    : invoice?.total || 0;

  const linkPath = isQuote
    ? `/quotes/${document.id}`
    : `/invoices/${document.id}`;

  return (
    <div
      className={cn(
        'bg-card border rounded-xl p-4 hover:shadow-md transition-shadow',
        className
      )}
    >
      
      <div className="flex items-start justify-between mb-3">
        <div>
          <Link
            to={linkPath}
            className="font-semibold text-foreground hover:text-primary transition-colors"
          >
            {isQuote ? quote?.number : invoice?.number}
          </Link>
          <p className="text-sm text-muted-foreground mt-0.5">
            {client?.name || client?.displayName || client?.legalName || 'Unknown Client'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <StatusBadge status={document.status} />
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreVertical className="w-4 h-4" />
                <span className="sr-only">Actions</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem asChild>
                <Link to={linkPath}>View Details</Link>
              </DropdownMenuItem>
              {onEdit && <DropdownMenuItem onClick={onEdit}>Edit</DropdownMenuItem>}
              {onDuplicate && (
                <DropdownMenuItem onClick={onDuplicate}>Duplicate</DropdownMenuItem>
              )}
              {onConvert && isQuote && quote?.status === 'accepted' && (
                <DropdownMenuItem onClick={onConvert}>
                  Convert to Invoice
                </DropdownMenuItem>
              )}
              {onDelete && (
                <DropdownMenuItem onClick={onDelete} className="text-destructive">
                  Delete
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      
      <p className="text-2xl font-bold mb-3">{formatCurrency(total)}</p>

      
      <div className="flex items-center gap-4 text-xs text-muted-foreground">
        <span className="flex items-center gap-1">
          <Calendar className="w-3.5 h-3.5" />
          {formatDate(document.createdAt || '')}
        </span>
        {!isQuote && invoice?.dueDate && (
          <span className="flex items-center gap-1">
            Due: {formatDate(invoice.dueDate)}
          </span>
        )}
        {isQuote && quote?.validUntil && (
          <span className="flex items-center gap-1">
            Valid until: {formatDate(quote.validUntil)}
          </span>
        )}
      </div>
    </div>
  );
}
