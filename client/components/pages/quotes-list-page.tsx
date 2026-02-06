'use client';

import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, Filter, FileText, MoreVertical } from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { StatusBadge } from '@/components/ui/status-badge';
import { EmptyState } from '@/components/ui/empty-state';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { services } from '@/lib/api-shim';
import type { QuoteSummaryResponse } from '@/lib/api-shim';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { QuoteStatus } from '@/lib/types';

const statusOptions: { value: QuoteStatus | 'all'; label: string }[] = [
  { value: 'all', label: 'All Statuses' },
  { value: 'draft', label: 'Draft' },
  { value: 'sent', label: 'Sent' },
  { value: 'accepted', label: 'Accepted' },
  { value: 'rejected', label: 'Rejected' },
  { value: 'expired', label: 'Expired' },
  { value: 'converted', label: 'Converted' },
  { value: 'archived', label: 'Archived' },
];

type QuoteRow = {
  id: string;
  number: string;
  status: QuoteStatus;
  totalAmount: number;
  issueDate?: string | null;
  validUntil?: string | null;
  customerId?: string | null;
  customerDisplayName: string;
};

const mapQuoteStatus = (status: string): QuoteStatus => {
  switch (status) {
    case 'DRAFT':
      return 'draft';
    case 'SENT':
      return 'sent';
    case 'ACCEPTED':
      return 'accepted';
    case 'REJECTED':
      return 'rejected';
    case 'EXPIRED':
      return 'expired';
    case 'CONVERTED':
      return 'converted';
    case 'ARCHIVED':
      return 'archived';
    default:
      return 'draft';
  }
};

const formatQuoteNumber = (year: number | null, number: number | null, id: string) => {
  if (year && number !== null && number !== undefined) {
    return `QUO-${year}-${String(number).padStart(3, '0')}`;
  }
  return `QUOTE-${id}`;
};

const mapQuoteRow = (quote: QuoteSummaryResponse): QuoteRow => ({
  id: String(quote.id),
  number: formatQuoteNumber(quote.quoteYear ?? null, quote.quoteNumber ?? null, String(quote.id)),
  status: mapQuoteStatus(quote.status),
  totalAmount: Number(quote.totalAmount ?? 0),
  issueDate: quote.issueDate ?? null,
  validUntil: quote.validUntil ?? null,
  customerId: quote.customerId ? String(quote.customerId) : null,
  customerDisplayName: quote.customerDisplayName ?? 'Unknown',
});

export function QuotesListPage() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<QuoteStatus | 'all'>('all');
  const [quotes, setQuotes] = useState<QuoteRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    setError(null);
    services.quotes
      .listQuotes({ page: 0, size: 200, sort: 'issueDate,desc' })
      .then((page: { content: QuoteSummaryResponse[] }) => {
        if (!active) return;
        setQuotes(page.content.map(mapQuoteRow));
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load quotes.');
      })
      .finally(() => {
        if (!active) return;
        setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const filteredQuotes = useMemo(() => {
    return quotes
      .filter((quote) => {
        if (statusFilter !== 'all' && quote.status !== statusFilter) return false;
        if (!searchQuery) return true;

        const query = searchQuery.toLowerCase();
        return (
          quote.number.toLowerCase().includes(query) ||
          quote.customerDisplayName.toLowerCase().includes(query)
        );
      })
      .sort((a, b) => new Date(b.issueDate ?? 0).getTime() - new Date(a.issueDate ?? 0).getTime());
  }, [searchQuery, statusFilter, quotes]);

  return (
    <div className="flex-1">
      <TopBar
        title="Quotes"
        actions={
          <Button asChild size="sm">
            <Link to="/quotes/new">
              <Plus className="w-4 h-4 mr-2" />
              <span className="hidden sm:inline">New Quote</span>
              <span className="sm:hidden">New</span>
            </Link>
          </Button>
        }
      />

      <div className="p-4 lg:p-6 space-y-4">
        
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search quotes..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
          <Select
            value={statusFilter}
            onValueChange={(value) => setStatusFilter(value as QuoteStatus | 'all')}
          >
            <SelectTrigger className="w-full sm:w-[180px]">
              <Filter className="w-4 h-4 mr-2" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {statusOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {filteredQuotes.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No quotes found"
            description={
              error
                ? error
                : searchQuery || statusFilter !== 'all'
                  ? 'Try adjusting your filters'
                  : isLoading
                    ? 'Loading quotes...'
                    : "You haven't created any quotes yet. Create your first quote to get started."
            }
            action={
              !isLoading && !error && !searchQuery && statusFilter === 'all'
                ? {
                  label: 'Create Quote',
                  onClick: () => navigate('/quotes/new'),
                }
                : undefined
            }
          />
        ) : (
          <>
            
            <div className="lg:hidden space-y-3">
              {filteredQuotes.map((quote) => (
                <Card key={quote.id} className="p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <Link
                        to={`/quotes/${quote.id}`}
                        className="font-semibold hover:text-primary transition-colors"
                      >
                        {quote.number}
                      </Link>
                      <p className="text-sm text-muted-foreground">
                        {quote.customerDisplayName}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusBadge status={quote.status} />
                      <QuoteActions quote={quote} />
                    </div>
                  </div>
                  <p className="text-2xl font-bold mb-3">
                    {formatCurrency(quote.totalAmount)}
                  </p>
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>{quote.issueDate ? formatDate(quote.issueDate) : '-'}</span>
                    <span>{quote.validUntil ? `Valid until ${formatDate(quote.validUntil)}` : '-'}</span>
                  </div>
                </Card>
              ))}
            </div>

            
            <Card className="hidden lg:block">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Quote #</TableHead>
                    <TableHead>Client</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Amount</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Valid Until</TableHead>
                    <TableHead className="text-center">Version</TableHead>
                    <TableHead className="w-12"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredQuotes.map((quote) => (
                    <TableRow key={quote.id}>
                      <TableCell>
                        <Link
                          to={`/quotes/${quote.id}`}
                          className="font-medium hover:text-primary transition-colors"
                        >
                          {quote.number}
                        </Link>
                      </TableCell>
                      <TableCell>{quote.customerDisplayName}</TableCell>
                      <TableCell>
                        <StatusBadge status={quote.status} />
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        {formatCurrency(quote.totalAmount)}
                      </TableCell>
                      <TableCell>{quote.issueDate ? formatDate(quote.issueDate) : '-'}</TableCell>
                      <TableCell>{quote.validUntil ? formatDate(quote.validUntil) : '-'}</TableCell>
                      <TableCell className="text-center">-</TableCell>
                      <TableCell>
                        <QuoteActions quote={quote} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>
          </>
        )}
      </div>
    </div>
  );
}

function QuoteActions({ quote }: { quote: QuoteRow }) {
  const navigate = useNavigate();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="h-8 w-8">
          <MoreVertical className="w-4 h-4" />
          <span className="sr-only">Actions</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem asChild>
          <Link to={`/quotes/${quote.id}`}>View Details</Link>
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link to={`/quotes/${quote.id}/edit`}>Edit</Link>
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link to={`/quotes/${quote.id}/versions`}>Version History</Link>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={() => navigate(`/quotes/${quote.id}/preview?type=quote`)}>
          Preview PDF
        </DropdownMenuItem>
        {quote.status === 'accepted' && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuItem asChild>
              <Link to={`/invoices/new?quoteId=${quote.id}`}>
                Convert to Invoice
              </Link>
            </DropdownMenuItem>
          </>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
