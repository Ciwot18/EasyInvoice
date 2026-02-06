'use client';

import React from "react"

import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Bell, Menu, X, LogOut, Receipt } from 'lucide-react';
import { useAuth, isAuthBypassed } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet';

interface TopBarProps {
  title: string;
  showSearch?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  actions?: React.ReactNode;
}

export function TopBar({
  title,
  showSearch = false,
  searchPlaceholder = 'Search...',
  onSearch,
  actions,
}: TopBarProps) {
  const { user, logout, switchRole } = useAuth();
  const showBypassSwitcher = isAuthBypassed();
  const [searchQuery, setSearchQuery] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    onSearch?.(value);
  };

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
        return 'Admin';
      case 'company_manager':
        return 'Manager';
      case 'back_office_operator':
        return 'Operator';
      default:
        return role;
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b border-border">
      <div className="flex items-center justify-between h-16 px-4 lg:px-6">
        
        <div className="flex items-center gap-3">
          
          <Link to="/dashboard" className="lg:hidden flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <Receipt className="w-5 h-5 text-primary-foreground" />
            </div>
          </Link>
          <h1 className="text-lg font-semibold hidden sm:block">{title}</h1>
        </div>

        
        {showSearch && (
          <div className="hidden md:flex flex-1 max-w-md mx-4">
            <div className="relative w-full">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                type="search"
                placeholder={searchPlaceholder}
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                className="pl-9 bg-muted/50"
              />
            </div>
          </div>
        )}

        
        <div className="flex items-center gap-2">
          
          {actions}

          
          <DropdownMenu>
            <DropdownMenuTrigger asChild className="hidden lg:flex">
              <Button variant="ghost" className="gap-2 h-auto py-1.5 px-2">
                <Avatar className="h-8 w-8">
                  <AvatarFallback className="bg-primary text-primary-foreground text-xs">
                    {user ? getInitials(user.name) : 'U'}
                  </AvatarFallback>
                </Avatar>
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

          
          <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
            <SheetTrigger asChild className="lg:hidden">
              <Button variant="ghost" size="icon">
                <Menu className="w-5 h-5" />
                <span className="sr-only">Open menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-[280px]">
              <SheetHeader>
                <SheetTitle>Account</SheetTitle>
              </SheetHeader>
              <div className="mt-6 space-y-6">
                
                <div className="flex items-center gap-3 pb-4 border-b">
                  <Avatar className="h-12 w-12">
                    <AvatarFallback className="bg-primary text-primary-foreground">
                      {user ? getInitials(user.name) : 'U'}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="font-medium">{user?.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {user && getRoleBadge(user.role)}
                    </p>
                  </div>
                </div>

                
                {showSearch && (
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      type="search"
                      placeholder={searchPlaceholder}
                      value={searchQuery}
                      onChange={(e) => handleSearch(e.target.value)}
                      className="pl-9"
                    />
                  </div>
                )}

                
                {showBypassSwitcher && (
                  <div className="space-y-2">
                    <p className="text-sm font-medium text-muted-foreground">Switch Role (Dev)</p>
                    <div className="space-y-1">
                      <Button
                        variant="ghost"
                        className="w-full justify-start"
                        onClick={() => {
                          switchRole('platform_admin');
                          setMobileMenuOpen(false);
                        }}
                      >
                        Platform Admin
                      </Button>
                      <Button
                        variant="ghost"
                        className="w-full justify-start"
                        onClick={() => {
                          switchRole('company_manager');
                          setMobileMenuOpen(false);
                        }}
                      >
                        Company Manager
                      </Button>
                      <Button
                        variant="ghost"
                        className="w-full justify-start"
                        onClick={() => {
                          switchRole('back_office_operator');
                          setMobileMenuOpen(false);
                        }}
                      >
                        Back Office Operator
                      </Button>
                    </div>
                  </div>
                )}

                
                <Button
                  variant="destructive"
                  className="w-full"
                  onClick={() => {
                    logout();
                    setMobileMenuOpen(false);
                  }}
                >
                  <LogOut className="w-4 h-4 mr-2" />
                  Sign out
                </Button>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>

      
      <div className="sm:hidden px-4 pb-3 -mt-1">
        <h1 className="text-lg font-semibold">{title}</h1>
      </div>
    </header>
  );
}
