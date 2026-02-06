'use client';

import { useState, useEffect, useMemo, useRef } from 'react';
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { ArrowLeft, Save, Send, Loader2, Calendar } from 'lucide-react';
import { format, addDays } from 'date-fns';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ClientPicker } from '@/components/ui/client-picker';
import { LineItemsEditor } from '@/components/ui/line-items-editor';
import { Calendar as CalendarComponent } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { toast } from 'sonner';
import { cn, generateDocumentNumber } from '@/lib/utils';
import { services } from '@/lib/api-shim';
import type { QuoteDetailResponse, QuoteItemResponse } from '@/lib/api-shim';
import type { LineItem } from '@/lib/types';

const createEmptyLineItem = (): LineItem => ({
  id: `item_${Math.random().toString(36).substring(2, 11)}`,
  description: '',
  quantity: 1,
  unitPrice: 0,
  vatRate: 20,
  discount: 0,
  total: 0,
});

export function QuoteEditorPage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [isLoading, setIsLoading] = useState(false);
  const [clientId, setClientId] = useState<string | null>(null);
  const [validUntil, setValidUntil] = useState<Date>(addDays(new Date(), 30));
  const [notes, setNotes] = useState('');
  const [lineItems, setLineItems] = useState<LineItem[]>([createEmptyLineItem()]);
  const originalItemIds = useRef<Set<string>>(new Set());

  useEffect(() => {
    const prefilledClientId = searchParams.get('clientId');
    if (prefilledClientId) {
      setClientId(prefilledClientId);
    }

    if (!isNew && id) {
      (async () => {
        const quote: QuoteDetailResponse = await services.quotes.getQuote(id);
        setClientId(quote.customerId ? String(quote.customerId) : null);
        if (quote.validUntil) setValidUntil(new Date(quote.validUntil));
        if (quote.notes) setNotes(quote.notes);
        const items: QuoteItemResponse[] = await services.quoteItems.listItems(id);
        const mapped: LineItem[] = items.map((item) => ({
          id: String(item.id),
          description: item.description ?? '',
          quantity: item.quantity ?? 1,
          unitPrice: item.unitPrice ?? 0,
          vatRate: item.taxRate ?? 0,
          discount: item.discountType === 'PERCENT' ? item.discountValue ?? 0 : 0,
          total: item.lineTotalAmount ?? 0,
        }));
        originalItemIds.current = new Set(items.map((i) => String(i.id)));
        if (mapped.length > 0) setLineItems(mapped);
      })();
    }
  }, [id, isNew, searchParams]);

  const quoteNumber = useMemo(() => {
    if (!isNew && id) {
      return `QUOTE-${id}`;
    }
    return generateDocumentNumber('QUO', 0);
  }, [id, isNew]);

  const handleSave = async (send = false) => {
    if (!clientId) {
      toast.error('Please select a client');
      return;
    }

    if (lineItems.every((item) => !item.description)) {
      toast.error('Please add at least one line item');
      return;
    }

    setIsLoading(true);

    try {
      if (isNew) {
        const created = await services.quotes.createQuote({
          customerId: Number(clientId),
          notes,
          issueDate: format(new Date(), 'yyyy-MM-dd'),
          validUntil: format(validUntil, 'yyyy-MM-dd'),
          items: lineItems.map((item, index) => ({
            position: index + 1,
            description: item.description,
            notes: item.notes,
            quantity: item.quantity,
            unit: item.unit,
            unitPrice: item.unitPrice,
            taxRate: item.vatRate,
            discountType: item.discount ? 'PERCENT' : 'NONE',
            discountValue: item.discount ?? 0,
          })),
        });
        if (send) {
          await services.quotes.sendQuote(created.id);
        }
      } else if (id) {
        await services.quotes.updateQuote(id, {
          notes,
          validUntil: format(validUntil, 'yyyy-MM-dd'),
        });

        const currentIds = new Set(lineItems.map((i) => i.id));
        const removed = Array.from(originalItemIds.current).filter((id) => !currentIds.has(id));
        await Promise.all(
          removed.map((itemId) => services.quoteItems.deleteItem(id, itemId))
        );

        await Promise.all(
          lineItems.map((item, index) => {
            const payload = {
              position: index + 1,
              description: item.description,
              notes: item.notes,
              quantity: item.quantity,
              unit: item.unit,
              unitPrice: item.unitPrice,
              taxRate: item.vatRate,
              discountType: (item.discount ? 'PERCENT' : 'NONE') as 'PERCENT' | 'NONE',
              discountValue: item.discount ?? 0,
            };
            if (/^\d+$/.test(item.id)) {
              return services.quoteItems.updateItem(id, item.id, payload);
            }
            return services.quoteItems.addItem(id, payload);
          })
        );

        if (send) {
          await services.quotes.sendQuote(id);
        }
      }

      toast.success(
        send
          ? 'Quote sent successfully'
          : isNew
            ? 'Quote created successfully'
            : 'Quote updated successfully'
      );
      navigate('/quotes');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Failed to save quote.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex-1">
      <TopBar
        title={isNew ? 'New Quote' : `Edit ${quoteNumber}`}
        actions={
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleSave(false)}
              disabled={isLoading}
            >
              {isLoading ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <>
                  <Save className="w-4 h-4 mr-2" />
                  <span className="hidden sm:inline">Save Draft</span>
                  <span className="sm:hidden">Save</span>
                </>
              )}
            </Button>
            <Button size="sm" onClick={() => handleSave(true)} disabled={isLoading}>
              {isLoading ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <>
                  <Send className="w-4 h-4 mr-2" />
                  <span className="hidden sm:inline">Save & Send</span>
                  <span className="sm:hidden">Send</span>
                </>
              )}
            </Button>
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
          <CardHeader>
            <CardTitle>Quote Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
              
              <div className="space-y-2">
                <Label>Client *</Label>
                <ClientPicker value={clientId} onChange={setClientId} />
              </div>
          </CardContent>
        </Card>

        
        <Card>
          <CardHeader>
            <CardTitle>Line Items</CardTitle>
          </CardHeader>
          <CardContent>
            <LineItemsEditor items={lineItems} onChange={setLineItems} />
          </CardContent>
        </Card>

        
        <Card>
          <CardHeader>
            <CardTitle>Notes</CardTitle>
          </CardHeader>
          <CardContent>
            <Textarea
              placeholder="Add any notes or terms for this quote..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={4}
            />
          </CardContent>
        </Card>

        
        <div className="sm:hidden flex gap-2">
          <Button
            variant="outline"
            className="flex-1 bg-transparent"
            onClick={() => handleSave(false)}
            disabled={isLoading}
          >
            <Save className="w-4 h-4 mr-2" />
            Save Draft
          </Button>
          <Button className="flex-1" onClick={() => handleSave(true)} disabled={isLoading}>
            <Send className="w-4 h-4 mr-2" />
            Save & Send
          </Button>
        </div>
      </div>
    </div>
  );
}
