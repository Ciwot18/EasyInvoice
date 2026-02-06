'use client';

import { Link } from 'react-router-dom';
import {
  FileText,
  Receipt,
  Users,
  TrendingUp,
  ArrowRight,
  Plus,
  Clock,
  CheckCircle,
  AlertTriangle,
  DollarSign,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/ui/status-badge';
import { useAuth } from '@/lib/auth-context';
import {
  mockQuotes,
  mockInvoices,
  mockClients,
  mockActivity,
  calculateDashboardStats,
} from '@/lib/mock-data';
import { formatCurrency, formatDate } from '@/lib/utils';

export function DashboardPage() {
  const { user } = useAuth();
  const stats = calculateDashboardStats(mockQuotes, mockInvoices);

  const recentQuotes = mockQuotes.slice(0, 3);
  const recentInvoices = mockInvoices.slice(0, 3);

  const getClientName = (clientId: string) => {
    return mockClients.find((c) => c.id === clientId)?.name || 'Unknown';
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'invoice_paid':
        return <CheckCircle className="w-4 h-4 text-success" />;
      case 'quote_accepted':
        return <CheckCircle className="w-4 h-4 text-success" />;
      case 'invoice_created':
      case 'quote_created':
        return <Plus className="w-4 h-4 text-primary" />;
      case 'quote_sent':
        return <FileText className="w-4 h-4 text-primary" />;
      case 'client_added':
        return <Users className="w-4 h-4 text-primary" />;
      default:
        return <Clock className="w-4 h-4 text-muted-foreground" />;
    }
  };

  return (
    <div className="flex-1">
      <TopBar title="Dashboard" />

      <div className="p-4 lg:p-6 space-y-6">
        
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold">
              Welcome back, {user?.name.split(' ')[0]}
            </h2>
            <p className="text-muted-foreground">
              Here&apos;s what&apos;s happening with your business today.
            </p>
          </div>
          <div className="flex gap-2">
            <Button asChild variant="outline">
              <Link to="/quotes/new">
                <Plus className="w-4 h-4 mr-2" />
                New Quote
              </Link>
            </Button>
            <Button asChild>
              <Link to="/invoices/new">
                <Plus className="w-4 h-4 mr-2" />
                New Invoice
              </Link>
            </Button>
          </div>
        </div>

        
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Revenue</p>
                  <p className="text-2xl font-bold mt-1">
                    {formatCurrency(stats.totalRevenue)}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-success/10 flex items-center justify-center">
                  <DollarSign className="w-6 h-6 text-success" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Pending Quotes</p>
                  <p className="text-2xl font-bold mt-1">{stats.pendingQuotes}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <FileText className="w-6 h-6 text-primary" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Unpaid Invoices</p>
                  <p className="text-2xl font-bold mt-1">{stats.unpaidInvoices}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-warning/10 flex items-center justify-center">
                  <AlertTriangle className="w-6 h-6 text-warning" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Clients</p>
                  <p className="text-2xl font-bold mt-1">{mockClients.length}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-accent flex items-center justify-center">
                  <Users className="w-6 h-6 text-accent-foreground" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <div className="grid lg:grid-cols-2 gap-6">
          
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-base font-semibold">Recent Quotes</CardTitle>
              <Button asChild variant="ghost" size="sm">
                <Link to="/quotes">
                  View all
                  <ArrowRight className="w-4 h-4 ml-1" />
                </Link>
              </Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {recentQuotes.map((quote) => (
                <Link
                  key={quote.id}
                  to={`/quotes/${quote.id}`}
                  className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors"
                >
                  <div className="min-w-0">
                    <p className="font-medium truncate">{quote.number}</p>
                    <p className="text-sm text-muted-foreground truncate">
                      {getClientName(quote.clientId)}
                    </p>
                  </div>
                  <StatusBadge status={quote.status} />
                </Link>
              ))}
            </CardContent>
          </Card>

          
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-base font-semibold">Recent Invoices</CardTitle>
              <Button asChild variant="ghost" size="sm">
                <Link to="/invoices">
                  View all
                  <ArrowRight className="w-4 h-4 ml-1" />
                </Link>
              </Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {recentInvoices.map((invoice) => (
                <Link
                  key={invoice.id}
                  to={`/invoices/${invoice.id}`}
                  className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors"
                >
                  <div className="min-w-0">
                    <p className="font-medium truncate">{invoice.number}</p>
                    <p className="text-sm text-muted-foreground truncate">
                      {getClientName(invoice.clientId)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">
                      {formatCurrency(invoice.total)}
                    </span>
                    <StatusBadge status={invoice.status} />
                  </div>
                </Link>
              ))}
            </CardContent>
          </Card>
        </div>

        
        <Card>
          <CardHeader>
            <CardTitle className="text-base font-semibold">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-3">
              <Button asChild variant="outline" className="h-auto py-4 flex-col gap-2 bg-transparent">
                <Link to="/quotes/new">
                  <FileText className="w-5 h-5" />
                  <span>Create Quote</span>
                </Link>
              </Button>
              <Button asChild variant="outline" className="h-auto py-4 flex-col gap-2 bg-transparent">
                <Link to="/invoices/new">
                  <Receipt className="w-5 h-5" />
                  <span>Create Invoice</span>
                </Link>
              </Button>
              <Button asChild variant="outline" className="h-auto py-4 flex-col gap-2 bg-transparent">
                <Link to="/clients/new">
                  <Users className="w-5 h-5" />
                  <span>Add Client</span>
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
