'use client';

import { useLocation, Link } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  FileText,
  Receipt,
  Settings,
  LogOut,
  ChevronDown,
  Building2,
  BarChart3,
  UserCog,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuth, usePermissions, isAuthBypassed } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';

const companyNavItems = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard, exact: true },
  { path: '/clients', label: 'Clients', icon: Users },
  { path: '/quotes', label: 'Quotes', icon: FileText },
  { path: '/invoices', label: 'Invoices', icon: Receipt },
];

const companySettingsItems = [
  { path: '/settings', label: 'Settings', icon: Settings, requiresManager: false },
  { path: '/users', label: 'Users', icon: Users, requiresManager: true },
];

const adminNavItems = [
  { path: '/admin', label: 'Dashboard', icon: LayoutDashboard, exact: true },
  { path: '/admin/companies', label: 'Companies', icon: Building2 },
  { path: '/admin/users', label: 'User Provisioning', icon: UserCog },
  { path: '/admin/monitoring', label: 'Monitoring', icon: BarChart3 },
];

export function DesktopSidebar() {
  const location = useLocation();
  const { user, logout, switchRole } = useAuth();
  const { isPlatformAdmin, canManageUsers } = usePermissions();

  const showBypassSwitcher = isAuthBypassed();

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'platform_admin':
        return 'Platform Admin';
      case 'company_manager':
        return 'Manager';
      case 'back_office_operator':
        return 'Operator';
      default:
        return role;
    }
  };

  const navItems = isPlatformAdmin ? adminNavItems : companyNavItems;
  const homeLink = isPlatformAdmin ? '/admin' : '/';

  return (
    <aside className="hidden lg:flex w-64 flex-col bg-sidebar text-sidebar-foreground border-r border-sidebar-border">
      
      <div className="h-16 flex items-center px-6 border-b border-sidebar-border">
        <Link to={homeLink} className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-sidebar-primary flex items-center justify-center">
            <Receipt className="w-5 h-5 text-sidebar-primary-foreground" />
          </div>
          <span className="text-lg font-semibold">EasyInvoice</span>
        </Link>
      </div>

      
      <nav className="flex-1 px-3 py-4 overflow-y-auto">
        
        {isPlatformAdmin && (
          <div className="px-3 mb-4">
            <Badge variant="outline" className="text-xs font-medium w-full justify-center py-1">
              Platform Admin
            </Badge>
          </div>
        )}

        <div className="space-y-1">
          {navItems.map((item) => {
            const isActive = 'exact' in item && item.exact
              ? location.pathname === item.path
              : location.pathname === item.path || location.pathname.startsWith(item.path + '/');
            return (
              <Link
                key={item.path}
                to={item.path}
                className={cn(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                    : 'text-sidebar-muted hover:bg-sidebar-accent/50 hover:text-sidebar-foreground'
                )}
              >
                <item.icon className="w-5 h-5" />
                {item.label}
              </Link>
            );
          })}
        </div>

        
        {!isPlatformAdmin && (
          <div className="mt-6 pt-6 border-t border-sidebar-border">
            <p className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-sidebar-muted">
              Settings
            </p>
            <div className="space-y-1">
              {companySettingsItems
                .filter((item) => !item.requiresManager || canManageUsers)
                .map((item) => {
                  const isActive = location.pathname === item.path;
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      className={cn(
                        'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                          : 'text-sidebar-muted hover:bg-sidebar-accent/50 hover:text-sidebar-foreground'
                      )}
                    >
                      <item.icon className="w-5 h-5" />
                      {item.label}
                    </Link>
                  );
                })}
            </div>
          </div>
        )}
      </nav>

      
      <div className="p-3 border-t border-sidebar-border">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="w-full justify-start gap-3 h-auto py-2 px-3 hover:bg-sidebar-accent"
            >
              <Avatar className="h-9 w-9">
                <AvatarFallback className="bg-sidebar-primary text-sidebar-primary-foreground text-sm">
                  {user ? getInitials(user.name) : 'U'}
                </AvatarFallback>
              </Avatar>
              <div className="flex-1 text-left">
                <p className="text-sm font-medium text-sidebar-foreground">{user?.name}</p>
                <p className="text-xs text-sidebar-muted">{user && getRoleBadge(user.role)}</p>
              </div>
              <ChevronDown className="w-4 h-4 text-sidebar-muted" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <div className="px-2 py-1.5">
              <p className="text-sm font-medium">{user?.name}</p>
              <p className="text-xs text-muted-foreground">{user?.email}</p>
            </div>
            {showBypassSwitcher && (
              <>
                <DropdownMenuSeparator />
                <div className="px-2 py-1">
                  <p className="text-xs font-medium text-muted-foreground">Switch Role (Dev)</p>
                </div>
                <DropdownMenuItem onClick={() => switchRole('platform_admin')}>
                  Platform Admin
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => switchRole('company_manager')}>
                  Company Manager
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => switchRole('back_office_operator')}>
                  Back Office Operator
                </DropdownMenuItem>
              </>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={logout} className="text-destructive">
              <LogOut className="w-4 h-4 mr-2" />
              Sign out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </aside>
  );
}
