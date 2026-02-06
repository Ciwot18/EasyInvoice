'use client';

import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, Filter, Receipt, MoreVertical, Calendar } from 'lucide-react';
import { format, startOfMonth, endOfMonth, isWithinInterval, parseISO, isValid } from 'date-fns';
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
import type { InvoiceSummaryResponse } from '@/lib/api-shim';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { InvoiceStatus } from '@/lib/types';

const statusOptions: { value: InvoiceStatus | 'all'; label: string }[] = [
  { value: 'all', label: 'All Statuses' },
  { value: 'draft', label: 'Draft' },
  { value: 'issued', label: 'Issued' },
  { value: 'paid', label: 'Paid' },
  { value: 'overdue', label: 'Overdue' },
  { value: 'archived', label: 'Archived' },
];

type InvoiceRow = {
  id: string;
  number: string;
  status: InvoiceStatus;
  totalAmount: number;
  issueDate?: string | null;
  dueDate?: string | null;
  customerId?: string | null;
  customerDisplayName: string;
  currency?: string | null;
};

const mapInvoiceStatus = (status: string): InvoiceStatus => {
  switch (status) {
    case 'DRAFT':
      return 'draft';
    case 'ISSUED':
      return 'issued';
    case 'PAID':
      return 'paid';
    case 'OVERDUE':
      return 'overdue';
    case 'ARCHIVED':
      return 'archived';
    default:
      return 'draft';
  }
};

const formatInvoiceNumber = (year: number | null, number: number | null, id: string) => {
  if (year && number !== null && number !== undefined) {
    return `INV-${year}-${String(number).padStart(3, '0')}`;
  }
  return `INV-${id}`;
};

const mapInvoiceRow = (invoice: InvoiceSummaryResponse): InvoiceRow => ({
  id: String(invoice.id),
  number: formatInvoiceNumber(invoice.invoiceYear ?? null, invoice.invoiceNumber ?? null, String(invoice.id)),
  status: mapInvoiceStatus(invoice.status),
  totalAmount: Number(invoice.totalAmount ?? 0),
  issueDate: invoice.issueDate ?? null,
  dueDate: invoice.dueDate ?? null,
  customerId: invoice.customerId ? String(invoice.customerId) : null,
  customerDisplayName: invoice.customerDisplayName ?? 'Unknown',
  currency: invoice.currency ?? null,
});

export function InvoicesListPage() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'all'>('all');
  const [monthFilter, setMonthFilter] = useState<string>('all');
  const [invoices, setInvoices] = useState<InvoiceRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadInvoices = () => {
    let active = true;
    setIsLoading(true);
    setError(null);
    services.invoices
      .listInvoices({ page: 0, size: 200, sort: 'issueDate,desc' })
      .then((page: { content: InvoiceSummaryResponse[] }) => {
        if (!active) return;
        setInvoices(page.content.map(mapInvoiceRow));
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load invoices.');
      })
      .finally(() => {
        if (!active) return;
        setIsLoading(false);
      });
    return () => {
      active = false;
    };
  };

  useEffect(() => loadInvoices(), []);

  const monthOptions = useMemo(() => {
    const months = new Map<string, string>();
    invoices.forEach((invoice) => {
      if (!invoice.issueDate) return;
      const date = parseISO(invoice.issueDate);
      if (!isValid(date)) return;
      const key = format(date, 'yyyy-MM');
      const label = format(date, 'MMMM yyyy');
      months.set(key, label);
    });
    const sorted = Array.from(months.entries()).sort((a, b) => b[0].localeCompare(a[0]));
    return [{ value: 'all', label: 'All Months' }, ...sorted.map(([value, label]) => ({ value, label }))];
  }, [invoices]);

  const filteredInvoices = useMemo(() => {
    return invoices
      .filter((invoice) => {
        if (statusFilter !== 'all' && invoice.status !== statusFilter) return false;

        if (monthFilter !== 'all') {
          const [year, month] = monthFilter.split('-').map(Number);
          if (!invoice.issueDate) return false;
          const invoiceDate = parseISO(invoice.issueDate);
          const monthStart = startOfMonth(new Date(year, month - 1));
          const monthEnd = endOfMonth(new Date(year, month - 1));
          if (!isWithinInterval(invoiceDate, { start: monthStart, end: monthEnd })) {
            return false;
          }
        }

        if (!searchQuery) return true;
        const query = searchQuery.toLowerCase();
        return (
          invoice.number.toLowerCase().includes(query) ||
          invoice.customerDisplayName.toLowerCase().includes(query)
        );
      })
      .sort((a, b) => new Date(b.issueDate ?? 0).getTime() - new Date(a.issueDate ?? 0).getTime());
  }, [searchQuery, statusFilter, monthFilter, invoices]);

  return (
    <div className="flex-1">
      <TopBar
        title="Invoices"
        actions={
          <Button asChild size="sm">
            <Link to="/invoices/new">
              <Plus className="w-4 h-4 mr-2" />
              <span className="hidden sm:inline">New Invoice</span>
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
              placeholder="Search invoices..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
          <Select
            value={statusFilter}
            onValueChange={(value) => setStatusFilter(value as InvoiceStatus | 'all')}
          >
            <SelectTrigger className="w-full sm:w-[160px]">
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
          <Select value={monthFilter} onValueChange={setMonthFilter}>
            <SelectTrigger className="w-full sm:w-[160px]">
              <Calendar className="w-4 h-4 mr-2" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {monthOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {filteredInvoices.length === 0 ? (
          <EmptyState
            icon={Receipt}
            title="No invoices found"
            description={
              error
                ? error
                : searchQuery || statusFilter !== 'all' || monthFilter !== 'all'
                  ? 'Try adjusting your filters'
                  : isLoading
                    ? 'Loading invoices...'
                    : "You haven't created any invoices yet. Create your first invoice to get started."
            }
            action={
              !isLoading && !error && !searchQuery && statusFilter === 'all' && monthFilter === 'all'
                ? {
                  label: 'Create Invoice',
                  onClick: () => navigate('/invoices/new'),
                }
                : undefined
            }
          />
        ) : (
          <>
            
            <div className="lg:hidden space-y-3">
              {filteredInvoices.map((invoice) => (
                <Card key={invoice.id} className="p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <Link
                        to={`/invoices/${invoice.id}`}
                        className="font-semibold hover:text-primary transition-colors"
                      >
                        {invoice.number}
                      </Link>
                      <p className="text-sm text-muted-foreground">
                        {invoice.customerDisplayName}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusBadge status={invoice.status} />
                      <InvoiceActions invoice={invoice} onChanged={loadInvoices} />
                    </div>
                  </div>
                  <p className="text-2xl font-bold mb-3">
                    {formatCurrency(invoice.totalAmount, invoice.currency ?? undefined)}
                  </p>
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>{invoice.issueDate ? formatDate(invoice.issueDate) : '-'}</span>
                    <span>Due: {invoice.dueDate ? formatDate(invoice.dueDate) : '-'}</span>
                  </div>
                </Card>
              ))}
            </div>

            
            <Card className="hidden lg:block">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Invoice #</TableHead>
                    <TableHead>Client</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Amount</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Due Date</TableHead>
                    <TableHead className="w-12"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredInvoices.map((invoice) => (
                    <TableRow key={invoice.id}>
                      <TableCell>
                        <Link
                          to={`/invoices/${invoice.id}`}
                          className="font-medium hover:text-primary transition-colors"
                        >
                          {invoice.number}
                        </Link>
                      </TableCell>
                      <TableCell>{invoice.customerDisplayName}</TableCell>
                      <TableCell>
                        <StatusBadge status={invoice.status} />
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        {formatCurrency(invoice.totalAmount, invoice.currency ?? undefined)}
                      </TableCell>
                      <TableCell>{invoice.issueDate ? formatDate(invoice.issueDate) : '-'}</TableCell>
                      <TableCell>{invoice.dueDate ? formatDate(invoice.dueDate) : '-'}</TableCell>
                      <TableCell>
                        <InvoiceActions invoice={invoice} onChanged={loadInvoices} />
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

function InvoiceActions({ invoice, onChanged }: { invoice: InvoiceRow; onChanged: () => void }) {
  const navigate = useNavigate();
  const [isBusy, setIsBusy] = useState(false);

  const handleMarkPaid = async () => {
    setIsBusy(true);
    try {
      await services.invoices.payInvoice(invoice.id);
      onChanged();
    } finally {
      setIsBusy(false);
    }
  };

  const handleDownloadPdf = async () => {
    setIsBusy(true);
    try {
      const blob = await services.invoices.getInvoicePdfDownload(invoice.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${invoice.number}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } finally {
      setIsBusy(false);
    }
  };

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
          <Link to={`/invoices/${invoice.id}`}>View Details</Link>
        </DropdownMenuItem>
        {invoice.status === 'draft' && (
          <DropdownMenuItem asChild>
            <Link to={`/invoices/${invoice.id}/edit`}>Edit</Link>
          </DropdownMenuItem>
        )}
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={() => navigate(`/invoices/${invoice.id}/preview?type=invoice`)}>
          Preview PDF
        </DropdownMenuItem>
        <DropdownMenuItem onClick={handleDownloadPdf} disabled={isBusy}>
          Download PDF
        </DropdownMenuItem>
        {(invoice.status === 'issued' || invoice.status === 'overdue') && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleMarkPaid} disabled={isBusy}>
              Mark as Paid
            </DropdownMenuItem>
          </>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
