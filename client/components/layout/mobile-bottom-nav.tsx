'use client';

import { useLocation, Link } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  FileText,
  Receipt,
  Settings,
  Building2,
  UserCog,
  BarChart3,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePermissions } from '@/lib/auth-context';

const companyNavItems = [
  { path: '/', label: 'Home', icon: LayoutDashboard, exact: true },
  { path: '/clients', label: 'Clients', icon: Users },
  { path: '/quotes', label: 'Quotes', icon: FileText },
  { path: '/invoices', label: 'Invoices', icon: Receipt },
  { path: '/settings', label: 'Settings', icon: Settings },
];

const adminNavItems = [
  { path: '/admin', label: 'Home', icon: LayoutDashboard, exact: true },
  { path: '/admin/companies', label: 'Companies', icon: Building2 },
  { path: '/admin/users', label: 'Users', icon: UserCog },
  { path: '/admin/monitoring', label: 'Monitor', icon: BarChart3 },
];

export function MobileBottomNav() {
  const location = useLocation();
  const { isPlatformAdmin } = usePermissions();

  const navItems = isPlatformAdmin ? adminNavItems : companyNavItems;

  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 z-50 bg-background border-t border-border safe-area-bottom">
      <div className="flex items-center justify-around h-16 px-2">
        {navItems.map((item) => {
          const isActive = 'exact' in item && item.exact 
            ? location.pathname === item.path
            : location.pathname === item.path || location.pathname.startsWith(item.path + '/');
          return (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                'flex flex-col items-center justify-center flex-1 h-full gap-1 transition-colors',
                isActive
                  ? 'text-primary'
                  : 'text-muted-foreground'
              )}
            >
              <item.icon className={cn('w-5 h-5', isActive && 'stroke-[2.5px]')} />
              <span className="text-[10px] font-medium">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
