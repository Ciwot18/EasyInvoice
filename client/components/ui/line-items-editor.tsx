'use client';

import { useState } from 'react';
import { Plus, Trash2, GripVertical } from 'lucide-react';
import { cn, formatCurrency } from '@/lib/utils';
import { Button } from './button';
import { Input } from './input';
import { Label } from './label';
import { Card, CardContent } from './card';
import type { LineItem } from '@/lib/types';

interface LineItemsEditorProps {
  items: LineItem[];
  onChange: (items: LineItem[]) => void;
  readOnly?: boolean;
}

const generateItemId = () => `item_${Math.random().toString(36).substring(2, 11)}`;

const createEmptyItem = (): LineItem => ({
  id: generateItemId(),
  description: '',
  quantity: 1,
  unitPrice: 0,
  vatRate: 20,
  discount: 0,
  total: 0,
});

const calculateItemTotal = (item: LineItem): number => {
  const gross = item.quantity * item.unitPrice;
  const discountAmount = gross * (item.discount / 100);
  return gross - discountAmount;
};

export function LineItemsEditor({ items, onChange, readOnly = false }: LineItemsEditorProps) {
  const updateItem = (id: string, updates: Partial<LineItem>) => {
    const newItems = items.map((item) => {
      if (item.id !== id) return item;
      const updated = { ...item, ...updates };
      updated.total = calculateItemTotal(updated);
      return updated;
    });
    onChange(newItems);
  };

  const addItem = () => {
    onChange([...items, createEmptyItem()]);
  };

  const removeItem = (id: string) => {
    if (items.length === 1) return;
    onChange(items.filter((item) => item.id !== id));
  };

  const subtotal = items.reduce((sum, item) => sum + item.total, 0);
  const vatTotal = items.reduce((sum, item) => sum + (item.total * item.vatRate / 100), 0);
  const total = subtotal + vatTotal;

  return (
    <div className="space-y-4">
      
      <div
        className="hidden lg:grid gap-3 px-4 text-sm font-medium text-muted-foreground"
        style={{ gridTemplateColumns: '1fr 100px 120px 80px 80px 100px 40px' }}
      >
        <span>Description</span>
        <span className="text-right">Qty</span>
        <span className="text-right">Unit Price</span>
        <span className="text-right">VAT %</span>
        <span className="text-right">Disc %</span>
        <span className="text-right">Total</span>
        <span></span>
      </div>

      
      <div className="space-y-3">
        {items.map((item, index) => (
          <LineItemRow
            key={item.id}
            item={item}
            index={index}
            onUpdate={(updates) => updateItem(item.id, updates)}
            onRemove={() => removeItem(item.id)}
            canRemove={items.length > 1}
            readOnly={readOnly}
          />
        ))}
      </div>

      
      {!readOnly && (
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={addItem}
          className="w-full sm:w-auto bg-transparent"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Line Item
        </Button>
      )}

      
      <div className="border-t pt-4 mt-6">
        <div className="flex flex-col items-end gap-2">
          <div className="flex items-center justify-between w-full sm:w-64 text-sm">
            <span className="text-muted-foreground">Subtotal</span>
            <span className="font-medium">{formatCurrency(subtotal)}</span>
          </div>
          <div className="flex items-center justify-between w-full sm:w-64 text-sm">
            <span className="text-muted-foreground">VAT</span>
            <span className="font-medium">{formatCurrency(vatTotal)}</span>
          </div>
          <div className="flex items-center justify-between w-full sm:w-64 text-lg pt-2 border-t">
            <span className="font-semibold">Total</span>
            <span className="font-bold">{formatCurrency(total)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

interface LineItemRowProps {
  item: LineItem;
  index: number;
  onUpdate: (updates: Partial<LineItem>) => void;
  onRemove: () => void;
  canRemove: boolean;
  readOnly: boolean;
}

function LineItemRow({
  item,
  index,
  onUpdate,
  onRemove,
  canRemove,
  readOnly,
}: LineItemRowProps) {
  const mobileView = (
    <Card className="lg:hidden">
      <CardContent className="pt-4 space-y-3">
        <div className="flex items-start justify-between">
          <span className="text-sm font-medium text-muted-foreground">
            Item {index + 1}
          </span>
          {!readOnly && canRemove && (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={onRemove}
              className="h-8 w-8 text-destructive"
            >
              <Trash2 className="w-4 h-4" />
              <span className="sr-only">Remove item</span>
            </Button>
          )}
        </div>

        <div className="flex items-center gap-3">
          <Label htmlFor={`desc-${item.id}`} className="w-20 shrink-0 text-sm text-muted-foreground">Description</Label>
          <Input
            id={`desc-${item.id}`}
            value={item.description}
            onChange={(e) => onUpdate({ description: e.target.value })}
            placeholder="Service or product description"
            disabled={readOnly}
          />
        </div>

        <div className="grid grid-cols-2 gap-x-4 gap-y-3">
          <div className="flex items-center gap-3">
            <Label htmlFor={`qty-${item.id}`} className="w-12 shrink-0 text-sm text-muted-foreground">Qty</Label>
            <Input
              id={`qty-${item.id}`}
              type="number"
              min="1"
              value={item.quantity}
              onChange={(e) => onUpdate({ quantity: Number(e.target.value) || 1 })}
              disabled={readOnly}
            />
          </div>
          <div className="flex items-center gap-3">
            <Label htmlFor={`price-${item.id}`} className="w-12 shrink-0 text-sm text-muted-foreground">Price</Label>
            <Input
              id={`price-${item.id}`}
              type="number"
              min="0"
              step="0.01"
              value={item.unitPrice}
              onChange={(e) => onUpdate({ unitPrice: Number(e.target.value) || 0 })}
              disabled={readOnly}
            />
          </div>
          <div className="flex items-center gap-3">
            <Label htmlFor={`vat-${item.id}`} className="w-12 shrink-0 text-sm text-muted-foreground">VAT %</Label>
            <Input
              id={`vat-${item.id}`}
              type="number"
              min="0"
              max="100"
              value={item.vatRate}
              onChange={(e) => onUpdate({ vatRate: Number(e.target.value) || 0 })}
              disabled={readOnly}
            />
          </div>
          <div className="flex items-center gap-3">
            <Label htmlFor={`disc-${item.id}`} className="w-12 shrink-0 text-sm text-muted-foreground">Disc %</Label>
            <Input
              id={`disc-${item.id}`}
              type="number"
              min="0"
              max="100"
              value={item.discount}
              onChange={(e) => onUpdate({ discount: Number(e.target.value) || 0 })}
              disabled={readOnly}
            />
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t">
          <span className="text-sm text-muted-foreground">Line Total</span>
          <span className="font-semibold">{formatCurrency(item.total)}</span>
        </div>
      </CardContent>
    </Card>
  );

  const desktopView = (
    <div
      className="hidden lg:grid gap-3 items-center p-3 rounded-lg border bg-card"
      style={{ gridTemplateColumns: '1fr 100px 120px 80px 80px 100px 40px' }}
    >
      <Input
        value={item.description}
        onChange={(e) => onUpdate({ description: e.target.value })}
        placeholder="Description"
        disabled={readOnly}
      />
      <Input
        type="number"
        min="1"
        value={item.quantity}
        onChange={(e) => onUpdate({ quantity: Number(e.target.value) || 1 })}
        className="text-right"
        disabled={readOnly}
      />
      <Input
        type="number"
        min="0"
        step="0.01"
        value={item.unitPrice}
        onChange={(e) => onUpdate({ unitPrice: Number(e.target.value) || 0 })}
        className="text-right"
        disabled={readOnly}
      />
      <Input
        type="number"
        min="0"
        max="100"
        value={item.vatRate}
        onChange={(e) => onUpdate({ vatRate: Number(e.target.value) || 0 })}
        className="text-right"
        disabled={readOnly}
      />
      <Input
        type="number"
        min="0"
        max="100"
        value={item.discount}
        onChange={(e) => onUpdate({ discount: Number(e.target.value) || 0 })}
        className="text-right"
        disabled={readOnly}
      />
      <span className="text-right font-medium pr-2">{formatCurrency(item.total)}</span>
      {!readOnly && canRemove ? (
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={onRemove}
          className="h-8 w-8 text-muted-foreground hover:text-destructive"
        >
          <Trash2 className="w-4 h-4" />
          <span className="sr-only">Remove item</span>
        </Button>
      ) : (
        <div className="w-8" />
      )}
    </div>
  );

  return (
    <>
      {mobileView}
      {desktopView}
    </>
  );
}
