'use client';

import React from "react"

import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save, Loader2 } from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { services } from '@/lib/api-shim';
import type { CustomerDetailResponse } from '@/lib/api-shim';
import type { Client } from '@/lib/types';

export function ClientEditorPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<Client>>({
    legalName: '',
    displayName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    country: '',
    vatNumber: '',
    notes: '',
  });

  useEffect(() => {
    if (!isNew && id) {
      services.customers.getCustomer(id).then((client: CustomerDetailResponse) => {
        setFormData({
          legalName: client.legalName ?? '',
          displayName: client.displayName ?? '',
          email: client.email ?? '',
          phone: client.phone ?? '',
          address: client.address ?? '',
          city: client.city ?? '',
          country: client.country ?? '',
          vatNumber: client.vatNumber ?? '',
          notes: '',
        });
      });
    }
  }, [id, isNew]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.displayName || !formData.vatNumber) {
      toast.error('Display name and VAT number are required.');
      return;
    }
    setIsLoading(true);

    try {
      if (isNew) {
        await services.customers.createCustomer({
          displayName: formData.displayName,
          legalName: formData.legalName || undefined,
          vatNumber: formData.vatNumber,
          email: formData.email || undefined,
          phone: formData.phone || undefined,
          address: formData.address || undefined,
          city: formData.city || undefined,
          country: formData.country || undefined,
        });
      } else if (id) {
        await services.customers.updateCustomer(id, {
          displayName: formData.displayName || undefined,
          legalName: formData.legalName || undefined,
          vatNumber: formData.vatNumber || undefined,
          email: formData.email || undefined,
          phone: formData.phone || undefined,
          address: formData.address || undefined,
          city: formData.city || undefined,
          country: formData.country || undefined,
        });
      }
      toast.success(isNew ? 'Client created successfully' : 'Client updated successfully');
      navigate('/clients');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Failed to save client.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex-1">
      <TopBar title={isNew ? 'New Client' : 'Edit Client'} />

      <div className="p-4 lg:p-6 max-w-2xl">
        
        <Button asChild variant="ghost" size="sm" className="-ml-2 mb-6">
          <Link to="/clients">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Clients
          </Link>
        </Button>

        <form onSubmit={handleSubmit}>
          <Card>
            <CardHeader>
              <CardTitle>Client Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              
              <div className="space-y-2">
                <Label htmlFor="name">Company Name *</Label>
                <Input
                  id="legalName"
                  name="legalName"
                  value={formData.legalName}
                  onChange={handleChange}
                  placeholder="Enter company name"
                  required
                />
              </div>

              
              <div className="space-y-2">
                <Label htmlFor="name">Display Name *</Label>
                <Input
                  id="displayName"
                  name="displayName"
                  value={formData.displayName}
                  onChange={handleChange}
                  placeholder="Enter company name"
                  required
                />
              </div>

              
              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email *</Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="billing@company.com"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone *</Label>
                  <Input
                    id="phone"
                    name="phone"
                    type="tel"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder="+1 (555) 123-4567"
                    required
                  />
                </div>
              </div>

              
              <div className="space-y-2">
                <Label htmlFor="address">Address *</Label>
                <Input
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="123 Business Street, Suite 100"
                  required
                />
              </div>

              
              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="city">City *</Label>
                  <Input
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleChange}
                    placeholder="San Francisco"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="country">Country *</Label>
                  <Input
                    id="country"
                    name="country"
                    value={formData.country}
                    onChange={handleChange}
                    placeholder="United States"
                    required
                  />
                </div>
              </div>

              
              <div className="space-y-2">
                <Label htmlFor="vatNumber">VAT Number</Label>
                <Input
                  id="vatNumber"
                  name="vatNumber"
                  value={formData.vatNumber || ''}
                  onChange={handleChange}
                  placeholder="US123456789"
                  required
                />
              </div>

              
              <div className="space-y-2">
                <Label htmlFor="notes">Notes (Optional)</Label>
                <Textarea
                  id="notes"
                  name="notes"
                  value={formData.notes || ''}
                  onChange={handleChange}
                  placeholder="Add any notes about this client..."
                  rows={4}
                />
              </div>

              
              <div className="flex items-center justify-end gap-3 pt-4 border-t">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/clients')}
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4 mr-2" />
                      {isNew ? 'Create Client' : 'Save Changes'}
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        </form>
      </div>
    </div>
  );
}
