'use client';

import { useEffect, useMemo, useState } from 'react';
import { Check, ChevronsUpDown, Search, Plus, Building2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from './button';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from './command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from './popover';
import { usePermissions } from '@/lib/auth-context';
import { services } from '@/lib/api-shim';
import type { CustomerSummaryResponse } from '@/lib/api-shim';
import type { Client } from '@/lib/types';

interface ClientPickerProps {
  value: string | null;
  onChange: (clientId: string | null) => void;
  disabled?: boolean;
  placeholder?: string;
}

export function ClientPicker({
  value,
  onChange,
  disabled = false,
  placeholder = 'Select a client...',
}: ClientPickerProps) {
  const [open, setOpen] = useState(false);
  const { canAccessClient } = usePermissions();

  const [clients, setClients] = useState<Client[]>([]);

  useEffect(() => {
    let active = true;
    services.customers
      .listCustomers({ page: 0, size: 200, sort: 'displayName,asc', type: 'summary' })
      .then((page: { content: CustomerSummaryResponse[] }) => {
        if (!active) return;
        const mapped: Client[] = page.content.map((c) => ({
          id: String(c.id),
          companyId: '',
          name: c.displayName ?? c.legalName ?? 'Unknown',
          displayName: c.displayName ?? undefined,
          legalName: c.legalName ?? undefined,
          email: '',
          phone: '',
          address: '',
          city: '',
          country: c.country ?? '',
          vatNumber: c.vatNumber ?? undefined,
          createdAt: new Date().toISOString(),
        }));
        setClients(mapped);
      })
      .catch(() => {
        if (active) setClients([]);
      });
    return () => {
      active = false;
    };
  }, []);

  const accessibleClients = useMemo(() => {
    return clients.filter((client) => canAccessClient(client.id));
  }, [canAccessClient, clients]);

  const selectedClient = useMemo(() => {
    return accessibleClients.find((client) => client.id === value);
  }, [value, accessibleClients]);

  const getClientLabel = (client: Client) =>
    client.name ?? client.displayName ?? client.legalName ?? 'Unknown';

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className={cn(
            'w-full justify-between',
            !selectedClient && 'text-muted-foreground'
          )}
          disabled={disabled}
        >
          {selectedClient ? (
            <span className="flex items-center gap-2 truncate">
              <Building2 className="w-4 h-4 shrink-0" />
              {getClientLabel(selectedClient)}
            </span>
          ) : (
            placeholder
          )}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[var(--radix-popover-trigger-width)] p-0" align="start">
        <Command>
          <CommandInput placeholder="Search clients..." />
          <CommandList>
            <CommandEmpty>
              <div className="py-6 text-center text-sm">
                <p className="text-muted-foreground mb-2">No clients found.</p>
                <Button variant="outline" size="sm" asChild>
                  <a href="/clients/new">
                    <Plus className="w-4 h-4 mr-2" />
                    Add Client
                  </a>
                </Button>
              </div>
            </CommandEmpty>
            <CommandGroup>
              {accessibleClients.map((client) => (
                <CommandItem
                  key={client.id}
                  value={getClientLabel(client)}
                  onSelect={() => {
                    onChange(client.id === value ? null : client.id);
                    setOpen(false);
                  }}
                >
                  <Check
                    className={cn(
                      'mr-2 h-4 w-4',
                      value === client.id ? 'opacity-100' : 'opacity-0'
                    )}
                  />
                  <div className="flex flex-col">
                    <span>{getClientLabel(client)}</span>
                    <span className="text-xs text-muted-foreground">
                      {client.email}
                    </span>
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
