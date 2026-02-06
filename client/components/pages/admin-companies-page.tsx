'use client';

import { useState, useCallback, useEffect } from 'react';
import {
  Building2,
  Search,
  ToggleLeft,
  ToggleRight,
  Users,
  Calendar,
  Mail,
  Plus,
  Phone,
  MapPin,
  Globe,
  FileText,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { services } from '@/lib/api-shim';
import type { CompanySummaryResponse } from '@/lib/api-shim';
import { formatDate } from '@/lib/utils';
import type { CompanySummary } from '@/lib/types';




interface CompanyFormData {
  name: string;
  vatNumber: string;
  address: string;
}

const EMPTY_FORM: CompanyFormData = {
  name: '',
  vatNumber: '',
  address: '',
};




export function AdminCompaniesPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [companies, setCompanies] = useState<CompanySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<CompanyFormData>(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState<Partial<Record<keyof CompanyFormData, string>>>({});

  const updateField = useCallback(
    <K extends keyof CompanyFormData>(key: K, value: CompanyFormData[K]) => {
      setForm((prev) => ({ ...prev, [key]: value }));
      setFormErrors((prev) => ({ ...prev, [key]: undefined }));
    },
    [],
  );

  const validateForm = (): boolean => {
    const errors: typeof formErrors = {};
    if (!form.name.trim()) errors.name = 'Company name is required'
    if (!form.vatNumber.trim()) errors.vatNumber = 'VAT number is required';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCreate = () => {
    if (!validateForm()) return;
    const payload = {
      name: form.name.trim(),
      vatNumber: form.vatNumber.trim(),
    };
    setError(null);
    services.companies
      .createCompany(payload)
      .then((resp: CompanySummaryResponse) => {
        const created: CompanySummary = {
          id: String(resp.id),
          name: resp.name,
          vatNumber: resp.vatNumber ?? payload.vatNumber,
          createdAt: resp.createdAt ?? new Date().toISOString(),
          enabled: true,
          userCount: 0,
        };
        setCompanies((prev) => [created, ...prev]);
        setForm(EMPTY_FORM);
        setFormErrors({});
        setDialogOpen(false);
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to create company.');
      });
  };

  const handleDialogOpenChange = (open: boolean) => {
    setDialogOpen(open);
    if (!open) {
      setForm(EMPTY_FORM);
      setFormErrors({});
    }
  };

  
  const filteredCompanies = companies.filter(
    (c) =>
      c.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (c.email ?? '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.vatNumber.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const handleToggleEnabled = (companyId: string) => {
    setCompanies((prev) =>
      prev.map((c) => (c.id === companyId ? { ...c, enabled: !c.enabled } : c)),
    );
  };

  const enabledCount = companies.filter((c) => c.enabled).length;
  const disabledCount = companies.filter((c) => !c.enabled).length;

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    setError(null);
    services.companies
      .listCompanies()
      .then((resp: CompanySummaryResponse[]) => {
        if (!active) return;
        const mapped: CompanySummary[] = resp.map((c) => ({
          id: String(c.id),
          name: c.name,
          vatNumber: c.vatNumber ?? '',
          createdAt: c.createdAt ?? undefined,
          enabled: true,
          userCount: 0,
        }));
        setCompanies(mapped);
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load companies.');
      })
      .finally(() => {
        if (!active) return;
        setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="flex-1">
      <TopBar title="Companies" />

      <div className="p-4 lg:p-6 space-y-6">
        
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-balance">Companies Management</h2>
            <p className="text-muted-foreground">
              View and manage registered companies on the platform.
            </p>
          </div>

          
          <Dialog open={dialogOpen} onOpenChange={handleDialogOpenChange}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                Create Company
              </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-lg">
              <DialogHeader>
                <DialogTitle>Create Company</DialogTitle>
                <DialogDescription>
                  Register a new company on the platform. You can assign users to it after creation.
                </DialogDescription>
              </DialogHeader>

              <div className="grid gap-4 py-4">
                
                <div className="grid gap-1.5">
                  <Label htmlFor="company-name">
                    Company Name <span className="text-destructive">*</span>
                  </Label>
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      id="company-name"
                      placeholder="Acme Corp"
                      value={form.name}
                      onChange={(e) => updateField('name', e.target.value)}
                      className="pl-9"
                    />
                  </div>
                  {formErrors.name && <p className="text-xs text-destructive">{formErrors.name}</p>}
                </div>

                
                <div className="grid gap-1.5">
                  <Label htmlFor="company-vat">
                    VAT Number <span className="text-destructive">*</span>
                  </Label>
                  <div className="relative">
                    <FileText className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      id="company-vat"
                      placeholder="US123456789"
                      value={form.vatNumber}
                      onChange={(e) => updateField('vatNumber', e.target.value)}
                      className="pl-9"
                    />
                  </div>
                  {formErrors.vatNumber && <p className="text-xs text-destructive">{formErrors.vatNumber}</p>}
                </div>

                
                <div className="grid gap-1.5">
                  <Label htmlFor="company-address">Address</Label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      id="company-address"
                      placeholder="123 Business St"
                      value={form.address}
                      onChange={(e) => updateField('address', e.target.value)}
                      className="pl-9"
                    />
                  </div>
                </div>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => handleDialogOpenChange(false)}>
                  Cancel
                </Button>
                <Button onClick={handleCreate}>Create Company</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Companies</p>
                  <p className="text-2xl font-bold mt-1">{companies.length}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <Building2 className="w-6 h-6 text-primary" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Active</p>
                  <p className="text-2xl font-bold mt-1">{enabledCount}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-emerald-500/10 flex items-center justify-center">
                  <ToggleRight className="w-6 h-6 text-emerald-500" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Disabled</p>
                  <p className="text-2xl font-bold mt-1">{disabledCount}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-destructive/10 flex items-center justify-center">
                  <ToggleLeft className="w-6 h-6 text-destructive" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {error && (
          <div className="rounded-md border border-destructive/50 bg-destructive/5 px-3 py-2 text-sm text-destructive">
            {error}
          </div>
        )}

        
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search companies..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        
        <div className="space-y-3">
          {isLoading ? (
            <div className="text-center py-12 text-muted-foreground">Loading companies…</div>
          ) : (
            filteredCompanies.map((company) => (
            <Card key={company.id}>
              <CardContent className="p-4">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-lg bg-muted flex items-center justify-center shrink-0">
                      <Building2 className="w-6 h-6 text-muted-foreground" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold truncate">{company.name}</h3>
                        <Badge variant={company.enabled ? 'default' : 'destructive'}>
                          {company.enabled ? 'Active' : 'Disabled'}
                        </Badge>
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 mt-1 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Mail className="w-3.5 h-3.5" />
                          {company.email ?? '—'}
                        </span>
                        <span className="flex items-center gap-1">
                          <Users className="w-3.5 h-3.5" />
                          {company.userCount ?? 0} users
                        </span>
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3.5 h-3.5" />
                          Registered {formatDate(company.createdAt ?? '')}
                        </span>
                      </div>
                      <p className="text-xs text-muted-foreground mt-1">
                        VAT: {company.vatNumber}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-2 shrink-0">
                    <Button
                      variant={company.enabled ? 'destructive' : 'default'}
                      size="sm"
                      onClick={() => handleToggleEnabled(company.id)}
                    >
                      {company.enabled ? (
                        <>
                          <ToggleLeft className="w-4 h-4 mr-1" />
                          Disable
                        </>
                      ) : (
                        <>
                          <ToggleRight className="w-4 h-4 mr-1" />
                          Enable
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
          )}

          {filteredCompanies.length === 0 && (
            <div className="text-center py-12 text-muted-foreground">
              No companies found matching your search.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
