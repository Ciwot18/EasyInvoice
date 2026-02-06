'use client';

import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, MoreVertical, Mail, Phone, MapPin, Building2 } from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { EmptyState } from '@/components/ui/empty-state';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
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
import { usePermissions } from '@/lib/auth-context';
import { services } from '@/lib/api-shim';
import type { CustomerDetailResponse, InvoiceSummaryResponse, QuoteSummaryResponse } from '@/lib/api-shim';
import type { Client } from '@/lib/types';

export function ClientsListPage() {
  const navigate = useNavigate();
  const { canAccessClient } = usePermissions();
  const [searchQuery, setSearchQuery] = useState('');
  const [clients, setClients] = useState<Client[]>([]);
  const [quoteCounts, setQuoteCounts] = useState<Record<string, number>>({});
  const [invoiceCounts, setInvoiceCounts] = useState<Record<string, number>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    setError(null);
    Promise.all([
      services.customers.listCustomers({ page: 0, size: 200, sort: 'displayName,asc' }),
      services.quotes.listQuotes({ page: 0, size: 500, sort: 'issueDate,desc' }),
      services.invoices.listInvoices({ page: 0, size: 500, sort: 'issueDate,desc' }),
    ])
      .then(([customersPage, quotesPage, invoicesPage]: [
        { content: CustomerDetailResponse[] },
        { content: QuoteSummaryResponse[] },
        { content: InvoiceSummaryResponse[] },
      ]) => {
        if (!active) return;
        const mapped: Client[] = customersPage.content.map((c) => ({
          id: String(c.id),
          companyId: '',
          name: c.displayName ?? c.legalName ?? 'Unknown',
          displayName: c.displayName ?? undefined,
          legalName: c.legalName ?? undefined,
          email: c.email ?? '',
          phone: c.phone ?? '',
          address: c.address ?? '',
          city: c.city ?? '',
          country: c.country ?? '',
          vatNumber: c.vatNumber ?? undefined,
          status: c.status ?? undefined,
          createdAt: c.createdAt ?? new Date().toISOString(),
        }));
        setClients(mapped);

        const qCounts: Record<string, number> = {};
        for (const q of quotesPage.content) {
          if (q.customerId == null) continue;
          const key = String(q.customerId);
          qCounts[key] = (qCounts[key] ?? 0) + 1;
        }
        setQuoteCounts(qCounts);

        const iCounts: Record<string, number> = {};
        for (const i of invoicesPage.content) {
          if (i.customerId == null) continue;
          const key = String(i.customerId);
          iCounts[key] = (iCounts[key] ?? 0) + 1;
        }
        setInvoiceCounts(iCounts);
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load clients.');
      })
      .finally(() => {
        if (!active) return;
        setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const filteredClients = useMemo(() => {
    return clients
      .filter((client) => canAccessClient(client.id))
      .filter((client) => {
        if (!searchQuery) return true;
        const query = searchQuery.toLowerCase();
        const name = client.name ?? client.displayName ?? client.legalName ?? '';
        const email = client.email ?? '';
        const city = client.city ?? '';
        return (
          name.toLowerCase().includes(query) ||
          email.toLowerCase().includes(query) ||
          city.toLowerCase().includes(query)
        );
      });
  }, [searchQuery, canAccessClient]);

  const getClientStats = (clientId: string) => {
    return {
      quotes: quoteCounts[clientId] ?? 0,
      invoices: invoiceCounts[clientId] ?? 0,
    };
  };

  return (
    <div className="flex-1">
      <TopBar
        title="Clients"
        actions={
          <Button asChild size="sm">
            <Link to="/clients/new">
              <Plus className="w-4 h-4 mr-2" />
              <span className="hidden sm:inline">Add Client</span>
              <span className="sm:hidden">Add</span>
            </Link>
          </Button>
        }
      />

      <div className="p-4 lg:p-6 space-y-4">
        
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search clients..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        {error && (
          <div className="rounded-md border border-destructive/50 bg-destructive/5 px-3 py-2 text-sm text-destructive">
            {error}
          </div>
        )}

        {isLoading ? (
          <div className="text-center py-12 text-muted-foreground">Loading clients…</div>
        ) : filteredClients.length === 0 ? (
          <EmptyState
            icon={Building2}
            title="No clients found"
            description={
              searchQuery
                ? 'Try adjusting your search terms'
                : "You haven't added any clients yet. Add your first client to get started."
            }
            action={
              !searchQuery
                ? {
                    label: 'Add Client',
                    onClick: () => navigate('/clients/new'),
                  }
                : undefined
            }
          />
        ) : (
          <>
            
            <div className="lg:hidden space-y-3">
              {filteredClients.map((client) => (
                <ClientCard
                  key={client.id}
                  client={client}
                  stats={getClientStats(client.id)}
                />
              ))}
            </div>

            
            <Card className="hidden lg:block">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Client</TableHead>
                    <TableHead>Contact</TableHead>
                    <TableHead>Country</TableHead>
                    <TableHead className="w-12"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredClients.map((client) => {
                    const stats = getClientStats(client.id);
                    return (
                      <TableRow key={client.id}>
                        <TableCell>
                          <Link
                            to={`/clients/${client.id}`}
                            className="font-medium hover:text-primary transition-colors"
                          >
                            {client.name ?? client.displayName ?? client.legalName ?? 'Unknown'}
                          </Link>
                          {client.vatNumber && (
                            <p className="text-xs text-muted-foreground">
                              VAT: {client.vatNumber}
                            </p>
                          )}
                        </TableCell>
                        <TableCell>
                          <div className="space-y-1">
                            <p className="text-sm flex items-center gap-1.5">
                              <Mail className="w-3.5 h-3.5 text-muted-foreground" />
                              {client.email ?? '—'}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell>
                          <p className="text-sm flex items-center gap-1.5">
                            <MapPin className="w-3.5 h-3.5 text-muted-foreground" />
                            {client.country ?? '—'}
                          </p>
                        </TableCell>
                        <TableCell>
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon" className="h-8 w-8">
                                <MoreVertical className="w-4 h-4" />
                                <span className="sr-only">Actions</span>
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem asChild>
                                <Link to={`/clients/${client.id}`}>View Details</Link>
                              </DropdownMenuItem>
                              <DropdownMenuItem asChild>
                                <Link to={`/clients/${client.id}/edit`}>Edit</Link>
                              </DropdownMenuItem>
                              <DropdownMenuItem asChild>
                                <Link to={`/quotes/new?clientId=${client.id}`}>
                                  Create Quote
                                </Link>
                              </DropdownMenuItem>
                              <DropdownMenuItem asChild>
                                <Link to={`/invoices/new?clientId=${client.id}`}>
                                  Create Invoice
                                </Link>
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </Card>
          </>
        )}
      </div>
    </div>
  );
}

function ClientCard({
  client,
  stats,
}: {
  client: Client;
  stats: { quotes: number; invoices: number };
}) {
  return (
    <Card>
      <CardContent className="pt-4">
        <div className="flex items-start justify-between mb-3">
          <div>
            <Link
              to={`/clients/${client.id}`}
              className="font-semibold hover:text-primary transition-colors"
            >
              {client.name ?? client.displayName ?? client.legalName ?? 'Unknown'}
            </Link>
            {client.vatNumber && (
              <p className="text-xs text-muted-foreground">VAT: {client.vatNumber}</p>
            )}
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreVertical className="w-4 h-4" />
                <span className="sr-only">Actions</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem asChild>
                <Link to={`/clients/${client.id}`}>View Details</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link to={`/clients/${client.id}/edit`}>Edit</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link to={`/quotes/new?clientId=${client.id}`}>Create Quote</Link>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div className="space-y-2 text-sm">
          <p className="flex items-center gap-2 text-muted-foreground">
            <Mail className="w-4 h-4" />
            {client.email ?? '—'}
          </p>
          <p className="flex items-center gap-2 text-muted-foreground">
            <MapPin className="w-4 h-4" />
            {client.country ?? '—'}
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
