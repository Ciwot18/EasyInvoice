'use client';

import { useEffect } from 'react';
import type { ReactNode } from 'react';
import { toast } from 'sonner';
import { DesktopSidebar } from './desktop-sidebar';
import { MobileBottomNav } from './mobile-bottom-nav';
import { Toaster } from '@/components/ui/sonner';
interface AppShellProps {
  children: ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  useEffect(() => {
    let cleanup: (() => void) | undefined;
    import('@/lib/api-shim').then(({ onApiError }) => {
      cleanup = onApiError((detail) => {
        if (detail.status === 401) return;
        toast.error(detail.message, {
          description: detail.path ? `Endpoint: ${detail.path}` : undefined,
        });
      });
    });
    return () => cleanup?.();
  }, []);

  return (
    <div className="flex min-h-screen bg-background">
      
      <DesktopSidebar />

      
      <main className="flex-1 flex flex-col min-h-screen pb-16 lg:pb-0">
        {children}
      </main>

      
      <MobileBottomNav />

      
      <Toaster position="top-right" />
    </div>
  );
}
