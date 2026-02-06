'use client';

import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Edit,
  Send,
  FileText,
  History,
  Receipt,
  Download,
  CheckCircle,
  XCircle,
  Archive,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/ui/status-badge';
import { EmptyState } from '@/components/ui/empty-state';
import { LineItemsEditor } from '@/components/ui/line-items-editor';
import { usePermissions } from '@/lib/auth-context';
import { formatCurrency, formatDate } from '@/lib/utils';
import { services } from '@/lib/api-shim';
import type { CustomerDetailResponse, QuoteDetailResponse, QuoteItemResponse } from '@/lib/api-shim';
import type { LineItem } from '@/lib/types';
import type { QuoteStatus } from '@/lib/types';

type QuoteView = {
  id: string;
  number: string;
  status: QuoteStatus;
  title?: string | null;
  notes?: string | null;
  issueDate?: string | null;
  subtotalAmount?: number | null;
  taxAmount?: number | null;
  totalAmount?: number | null;
  customerId?: string | null;
  customerDisplayName?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

const mapQuoteStatus = (status: QuoteDetailResponse["status"]): QuoteStatus => {
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

const mapQuoteView = (quote: QuoteDetailResponse): QuoteView => ({
  id: String(quote.id),
  number: formatQuoteNumber(quote.quoteYear ?? null, quote.quoteNumber ?? null, String(quote.id)),
  status: mapQuoteStatus(quote.status),
  title: quote.title ?? null,
  notes: quote.notes ?? null,
  issueDate: quote.issueDate ?? null,
  subtotalAmount: quote.subtotalAmount ?? null,
  taxAmount: quote.taxAmount ?? null,
  totalAmount: quote.totalAmount ?? null,
  customerId: quote.customerId ? String(quote.customerId) : null,
  customerDisplayName: quote.customerDisplayName ?? null,
  createdAt: quote.createdAt ?? null,
  updatedAt: quote.updatedAt ?? null,
});

const mapQuoteItems = (items: QuoteItemResponse[]): LineItem[] => {
  return items.map((item) => {
    const quantity = item.quantity ?? 0;
    const unitPrice = item.unitPrice ?? 0;
    const discountType = item.discountType ?? 'NONE';
    const discountValue = item.discountValue ?? 0;
    const gross = quantity * unitPrice;
    const discountPercent =
      discountType === 'PERCENT'
        ? discountValue
        : discountType === 'AMOUNT' && gross > 0
          ? (discountValue / gross) * 100
          : 0;

    return {
      id: String(item.id),
      description: item.description ?? '',
      quantity,
      unitPrice,
      vatRate: item.taxRate ?? 0,
      discount: Number.isFinite(discountPercent) ? discountPercent : 0,
      total: item.lineTotalAmount ?? gross,
    };
  });
};

export function QuoteDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canAccessClient, canEditDocument } = usePermissions();

  const [quote, setQuote] = useState<QuoteView | null>(null);
  const [items, setItems] = useState<LineItem[]>([]);
  const [customer, setCustomer] = useState<CustomerDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    if (!id) return;
    setIsLoading(true);
    setError(null);

    const load = async () => {
      try {
        const detail = await services.quotes.getQuote(id);
        const itemsResponse = await services.quoteItems.listItems(id);
        if (!active) return;
        const mapped = mapQuoteView(detail);
        setQuote(mapped);
        setItems(mapQuoteItems(itemsResponse));
        if (mapped.customerId) {
          const customerDetail = await services.customers.getCustomer(mapped.customerId);
          if (active) setCustomer(customerDetail);
        } else {
          setCustomer(null);
        }
      } catch (err) {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load quote.');
      } finally {
        if (active) setIsLoading(false);
      }
    };

    load();
    return () => {
      active = false;
    };
  }, [id]);

  if (!quote || (quote.customerId && !canAccessClient(quote.customerId))) {
    return (
      <div className="flex-1">
        <TopBar title="Quote Not Found" />
        <div className="p-4 lg:p-6">
          <EmptyState
            icon={FileText}
            title="Quote not found"
            description={error ?? (isLoading ? 'Loading quote...' : "The quote you're looking for doesn't exist or you don't have access to it.")}
            action={{
              label: 'Back to Quotes',
              onClick: () => navigate('/quotes'),
            }}
          />
        </div>
      </div>
    );
  }

  const canEdit = quote.customerId ? canEditDocument(quote.customerId) && quote.status === 'draft' : quote.status === 'draft';
  const canConvert = quote.status === 'accepted';

  const handleSend = async () => {
    if (!id) return;
    await services.quotes.sendQuote(id);
    const updated = await services.quotes.getQuote(id);
    setQuote(mapQuoteView(updated));
  };

  const handleAccept = async () => {
    if (!id) return;
    await services.quotes.acceptQuote(id);
    const updated = await services.quotes.getQuote(id);
    setQuote(mapQuoteView(updated));
  };

  const handleReject = async () => {
    if (!id) return;
    await services.quotes.rejectQuote(id);
    const updated = await services.quotes.getQuote(id);
    setQuote(mapQuoteView(updated));
  };

  const handleArchive = async () => {
    if (!id) return;
    await services.quotes.archiveQuote(id);
    const updated = await services.quotes.getQuote(id);
    setQuote(mapQuoteView(updated));
  };

  const handleConvert = async () => {
    if (!id) return;
    const invoice = await services.quotes.convertToInvoice(id);
    navigate(`/invoices/${invoice.id}`);
  };

  return (
    <div className="flex-1">
      <TopBar
        title={quote.number}
        actions={
          <div className="flex items-center gap-2">
            {canEdit && (
              <Button asChild variant="outline" size="sm">
                <Link to={`/quotes/${quote.id}/edit`}>
                  <Edit className="w-4 h-4 mr-2" />
                  <span className="hidden sm:inline">Edit</span>
                </Link>
              </Button>
            )}
            <Button asChild variant="outline" size="sm">
              <Link to={`/quotes/${quote.id}/preview?type=quote`}>
                <Download className="w-4 h-4 mr-2" />
                <span className="hidden sm:inline">PDF</span>
              </Link>
            </Button>
            {canConvert && (
              <Button size="sm" onClick={handleConvert}>
                <Receipt className="w-4 h-4 mr-2" />
                <span className="hidden sm:inline">Convert to Invoice</span>
                <span className="sm:hidden">Invoice</span>
              </Button>
            )}
          </div>
        }
      />

      <div className="p-4 lg:p-6 space-y-6 max-w-5xl">
        
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link to="/quotes">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Quotes
          </Link>
        </Button>

        
        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h2 className="text-2xl font-bold">{quote.number}</h2>
                  <StatusBadge status={quote.status} />
                </div>
                <p className="text-muted-foreground">{quote.title ?? 'Quote'}</p>
              </div>
              <div className="text-right">
                <p className="text-3xl font-bold">
                  {formatCurrency(quote.totalAmount ?? 0)}
                </p>
                <p className="text-sm text-muted-foreground">Total amount</p>
              </div>
            </div>
          </CardContent>
        </Card>

        
        <div className="grid sm:grid-cols-2 gap-6">
          
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Client</CardTitle>
            </CardHeader>
            <CardContent>
              {customer ? (
                <div className="space-y-2">
                  <Link
                    to={`/clients/${customer.id}`}
                    className="font-semibold hover:text-primary transition-colors"
                  >
                    {customer.displayName}
                  </Link>
                  <p className="text-sm text-muted-foreground">{customer.email ?? '-'}</p>
                  <p className="text-sm text-muted-foreground">{customer.phone ?? '-'}</p>
                  <p className="text-sm text-muted-foreground">
                    {customer.address ?? '-'}
                    <br />
                    {customer.city ?? '-'}, {customer.country ?? '-'}
                  </p>
                </div>
              ) : (
                <p className="text-muted-foreground">{quote.customerDisplayName ?? 'Client not found'}</p>
              )}
            </CardContent>
          </Card>

          
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Quote Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Created</span>
                <span className="text-sm font-medium">{quote.createdAt ? formatDate(quote.createdAt) : '-'}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Last Updated</span>
                <span className="text-sm font-medium">{quote.updatedAt ? formatDate(quote.updatedAt) : '-'}</span>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Line Items</CardTitle>
          </CardHeader>
          <CardContent>
            <LineItemsEditor
              items={items}
              onChange={() => { }}
              readOnly
            />
          </CardContent>
        </Card>

        
        {quote.notes && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Notes</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm whitespace-pre-wrap">{quote.notes}</p>
            </CardContent>
          </Card>
        )}

        
        <div className="flex flex-wrap gap-3">
          <Button asChild variant="outline">
            <Link to={`/quotes`}>
              <History className="w-4 h-4 mr-2" />
              Back to Quotes
            </Link>
          </Button>
        {quote.status === 'draft' && (
          <Button onClick={handleSend}>
            <Send className="w-4 h-4 mr-2" />
            Send to Client
          </Button>
        )}
        {quote.status === 'sent' && (
          <>
            <Button variant="outline" onClick={handleAccept}>
              <CheckCircle className="w-4 h-4 mr-2" />
              Accept
            </Button>
            <Button variant="outline" onClick={handleReject}>
              <XCircle className="w-4 h-4 mr-2" />
              Reject
            </Button>
          </>
        )}
        {quote.status !== 'archived' && (
          <Button variant="outline" onClick={handleArchive}>
            <Archive className="w-4 h-4 mr-2" />
            Archive
          </Button>
        )}
        </div>
      </div>
    </div>
  );
}
