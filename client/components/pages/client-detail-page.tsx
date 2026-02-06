'use client';

import { useEffect, useMemo, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Edit,
  Archive,
  RotateCcw,
  Trash2,
  Mail,
  Phone,
  MapPin,
  Building2,
  FileText,
  Receipt,
  Plus,
  ExternalLink,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/ui/status-badge';
import { EmptyState } from '@/components/ui/empty-state';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs';
import { usePermissions } from '@/lib/auth-context';
import { services } from '@/lib/api-shim';
import type { CustomerDetailResponse, InvoiceSummaryResponse } from '@/lib/api-shim';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { InvoiceStatus, QuoteStatus } from '@/lib/types';

export function ClientDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canAccessClient, canEditDocument } = usePermissions();

  const [client, setClient] = useState<CustomerDetailResponse | null>(null);
  const [quotes, setQuotes] = useState<
    Array<{
      id: number;
      quoteYear?: number | null;
      quoteNumber?: number | null;
      status?: string | null;
      issueDate?: string | null;
      validUntil?: string | null;
      totalAmount?: number | null;
    }>
  >([]);
  const [invoices, setInvoices] = useState<InvoiceSummaryResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let active = true;
    setIsLoading(true);
    setError(null);
    Promise.all([
      services.customers.getCustomer(id),
      services.customers.listCustomerQuotes(id),
      services.invoices.listInvoices({ page: 0, size: 200, sort: 'issueDate,desc' }),
    ])
      .then(([cust, custQuotes, invoicePage]) => {
        if (!active) return;
        setClient(cust as CustomerDetailResponse);
        setQuotes(custQuotes as any);
        setInvoices(
          (invoicePage as { content: InvoiceSummaryResponse[] }).content.filter(
            (i) => String(i.customerId) === String(id)
          )
        );
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load client.');
      })
      .finally(() => {
        if (!active) return;
        setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id]);

  if (isLoading) {
    return (
      <div className="flex-1">
        <TopBar title="Client" />
        <div className="p-4 lg:p-6 text-muted-foreground">Loading client…</div>
      </div>
    );
  }

  if (error || !client || !canAccessClient(String(client.id))) {
    return (
      <div className="flex-1">
        <TopBar title="Client Not Found" />
        <div className="p-4 lg:p-6">
          <EmptyState
            icon={Building2}
            title="Client not found"
            description={
              error ||
              "The client you're looking for doesn't exist or you don't have access to it."
            }
            action={{
              label: 'Back to Clients',
              onClick: () => navigate('/clients'),
            }}
          />
        </div>
      </div>
    );
  }

  const totalQuotesValue = quotes.reduce((sum, q) => sum + Number(q.totalAmount ?? 0), 0);
  const totalInvoicesValue = invoices.reduce((sum, i) => sum + Number(i.totalAmount ?? 0), 0);
  const paidInvoicesValue = invoices
    .filter((i) => i.status === 'PAID')
    .reduce((sum, i) => sum + Number(i.totalAmount ?? 0), 0);

  const mapQuoteStatus = (status: string | null | undefined): QuoteStatus => {
    switch (status) {
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

  const mapInvoiceStatus = (status: string | null | undefined): InvoiceStatus => {
    switch (status) {
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

  const handleArchive = async () => {
    if (!client) return;
    if (!window.confirm('Archive this client?')) return;
    try {
      await services.customers.archiveCustomer(client.id);
      const refreshed = await services.customers.getCustomer(client.id);
      setClient(refreshed);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to archive client.');
    }
  };

  const handleRestore = async () => {
    if (!client) return;
    try {
      await services.customers.restoreCustomer(client.id);
      const refreshed = await services.customers.getCustomer(client.id);
      setClient(refreshed);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to restore client.');
    }
  };

  const handleDelete = async () => {
    if (!client) return;
    if (!window.confirm('Delete this client? This action cannot be undone.')) return;
    try {
      await services.customers.deleteCustomer(client.id);
      navigate('/clients');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete client.');
    }
  };

  const formatQuoteNumber = (year: number | null, number: number | null, idVal: string) => {
    if (year && number !== null && number !== undefined) {
      return `QUO-${year}-${String(number).padStart(3, '0')}`;
    }
    return `QUOTE-${idVal}`;
  };

  const formatInvoiceNumber = (year: number | null, number: number | null, idVal: string) => {
    if (year && number !== null && number !== undefined) {
      return `INV-${year}-${String(number).padStart(3, '0')}`;
    }
    return `INV-${idVal}`;
  };

  return (
    <div className="flex-1">
      <TopBar
        title={client.legalName ?? client.displayName ?? 'Client'}
        actions={
          <div className="flex items-center gap-2">
            {canEditDocument(String(client.id)) && (
              <Button asChild variant="outline" size="sm">
                <Link to={`/clients/${client.id}/edit`}>
                  <Edit className="w-4 h-4 mr-2" />
                  Edit
                </Link>
              </Button>
            )}
            {client.status === 'ARCHIVED' ? (
              <Button variant="outline" size="sm" onClick={handleRestore}>
                <RotateCcw className="w-4 h-4 mr-2" />
                Restore
              </Button>
            ) : (
              <Button variant="outline" size="sm" onClick={handleArchive}>
                <Archive className="w-4 h-4 mr-2" />
                Archive
              </Button>
            )}
            <Button variant="destructive" size="sm" onClick={handleDelete}>
              <Trash2 className="w-4 h-4 mr-2" />
              Delete
            </Button>
          </div>
        }
      />

      <div className="p-4 lg:p-6 space-y-6">
        
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link to="/clients">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Clients
          </Link>
        </Button>

        
        <div className="grid lg:grid-cols-3 gap-6">
          
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-lg">Contact Information</CardTitle>
            </CardHeader>
            <CardContent className="grid sm:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Company Name</p>
                  <p className="font-medium flex items-center gap-2">
                    <Building2 className="w-4 h-4 text-muted-foreground" />
                    {client.legalName ?? client.displayName ?? '—'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Display Name</p>
                  <p className="font-medium flex items-center gap-2">
                    <Building2 className="w-4 h-4 text-muted-foreground" />
                    {client.displayName ?? client.legalName ?? '—'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Email</p>
                  <a
                    href={`mailto:${client.email ?? ''}`}
                    className="font-medium flex items-center gap-2 hover:text-primary transition-colors"
                  >
                    <Mail className="w-4 h-4 text-muted-foreground" />
                    {client.email ?? '—'}
                    <ExternalLink className="w-3 h-3" />
                  </a>
                </div>
              </div>
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Address</p>
                  <p className="font-medium flex items-start gap-2">
                    <MapPin className="w-4 h-4 text-muted-foreground mt-0.5" />
                    <span>
                      {client.address ?? '—'}
                      <br />
                      {client.city ?? '—'}, {client.country ?? '—'}
                    </span>
                  </p>
                </div>
                {client.vatNumber && (
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">VAT Number</p>
                    <p className="font-medium">{client.vatNumber}</p>
                  </div>
                )}
                
              </div>
            </CardContent>
          </Card>

          
          
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between py-2 border-b">
                <span className="text-muted-foreground">Total Quotes</span>
                <span className="font-semibold">{quotes.length}</span>
              </div>
              <div className="flex items-center justify-between py-2 border-b">
                <span className="text-muted-foreground">Quotes Value</span>
                <span className="font-semibold">{formatCurrency(totalQuotesValue)}</span>
              </div>
              <div className="flex items-center justify-between py-2 border-b">
                <span className="text-muted-foreground">Total Invoices</span>
                <span className="font-semibold">{invoices.length}</span>
              </div>
              <div className="flex items-center justify-between py-2 border-b">
                <span className="text-muted-foreground">Invoices Value</span>
                <span className="font-semibold">{formatCurrency(totalInvoicesValue)}</span>
              </div>
              <div className="flex items-center justify-between py-2">
                <span className="text-muted-foreground">Paid</span>
                <span className="font-semibold text-success">
                  {formatCurrency(paidInvoicesValue)}
                </span>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <Tabs defaultValue="quotes" className="w-full">
          <div className="flex items-center justify-between mb-4">
            <TabsList>
              <TabsTrigger value="quotes" className="gap-2">
                <FileText className="w-4 h-4" />
                Quotes ({quotes.length})
              </TabsTrigger>
              <TabsTrigger value="invoices" className="gap-2">
                <Receipt className="w-4 h-4" />
                Invoices ({invoices.length})
              </TabsTrigger>
            </TabsList>
            <div className="hidden sm:flex gap-2">
              <Button asChild variant="outline" size="sm">
                <Link to={`/quotes/new?clientId=${client.id}`}>
                  <Plus className="w-4 h-4 mr-2" />
                  New Quote
                </Link>
              </Button>
              <Button asChild size="sm">
                <Link to={`/invoices/new?clientId=${client.id}`}>
                  <Plus className="w-4 h-4 mr-2" />
                  New Invoice
                </Link>
              </Button>
            </div>
          </div>

          <TabsContent value="quotes" className="space-y-3">
            {quotes.length === 0 ? (
              <EmptyState
                icon={FileText}
                title="No quotes yet"
                description="Create your first quote for this client."
                action={{
                  label: 'Create Quote',
                  onClick: () => navigate(`/quotes/new?clientId=${client.id}`),
                }}
              />
            ) : (
              quotes.map((quote) => {
                const number = formatQuoteNumber(
                  quote.quoteYear ?? null,
                  quote.quoteNumber ?? null,
                  String(quote.id)
                );
                return (
                  <Card key={quote.id}>
                    <CardContent className="py-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <Link
                            to={`/quotes/${quote.id}`}
                            className="font-medium hover:text-primary transition-colors"
                          >
                            {number}
                          </Link>
                          <p className="text-sm text-muted-foreground">
                            {formatDate(quote.issueDate ?? '')}
                          </p>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-semibold">
                            {formatCurrency(quote.totalAmount ?? 0)}
                          </span>
                          <StatusBadge status={mapQuoteStatus(quote.status)} />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })
            )}
          </TabsContent>

          <TabsContent value="invoices" className="space-y-3">
            {invoices.length === 0 ? (
              <EmptyState
                icon={Receipt}
                title="No invoices yet"
                description="Create your first invoice for this client."
                action={{
                  label: 'Create Invoice',
                  onClick: () => navigate(`/invoices/new?clientId=${client.id}`),
                }}
              />
            ) : (
              invoices.map((invoice) => (
                <Card key={invoice.id}>
                  <CardContent className="py-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <Link
                          to={`/invoices/${invoice.id}`}
                          className="font-medium hover:text-primary transition-colors"
                        >
                          {formatInvoiceNumber(
                            invoice.invoiceYear ?? null,
                            invoice.invoiceNumber ?? null,
                            String(invoice.id)
                          )}
                        </Link>
                        <p className="text-sm text-muted-foreground">
                          {formatDate(invoice.issueDate ?? '')} · Due{' '}
                          {formatDate(invoice.dueDate ?? '')}
                        </p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="font-semibold">
                          {formatCurrency(invoice.totalAmount ?? 0)}
                        </span>
                        <StatusBadge status={mapInvoiceStatus(invoice.status)} />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </TabsContent>
        </Tabs>

        
        <div className="sm:hidden flex gap-2">
          <Button asChild variant="outline" className="flex-1 bg-transparent">
            <Link to={`/quotes/new?clientId=${client.id}`}>
              <Plus className="w-4 h-4 mr-2" />
              New Quote
            </Link>
          </Button>
          <Button asChild className="flex-1">
            <Link to={`/invoices/new?clientId=${client.id}`}>
              <Plus className="w-4 h-4 mr-2" />
              New Invoice
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
