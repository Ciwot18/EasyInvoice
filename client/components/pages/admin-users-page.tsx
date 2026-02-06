'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  Plus,
  Search,
  UserCog,
  Users,
  Mail,
  Building2,
  Shield,
  MoreHorizontal,
  UserX,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { services } from '@/lib/api-shim';
import type { CompanySummaryResponse, UserSummary } from '@/lib/api-shim';
import { formatDate } from '@/lib/utils';
import type { CompanySummary, CompanyUser, UserRole } from '@/lib/types';

export function AdminUsersPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [companyFilter, setCompanyFilter] = useState<string>('all');
  const [users, setUsers] = useState<CompanyUser[]>([]);
  const [companies, setCompanies] = useState<CompanySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [newUserEmail, setNewUserEmail] = useState('');
  const [newUserName, setNewUserName] = useState('');
  const [newUserCompanyId, setNewUserCompanyId] = useState('');
  const [newUserPassword, setNewUserPassword] = useState('');

  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.companyName.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCompany = companyFilter === 'all' || u.companyId === companyFilter;
    return matchesSearch && matchesCompany;
  });

  const enabledCompanies = useMemo(
    () => companies.filter((c) => c.enabled !== false),
    [companies]
  );

  const handleCreateUser = () => {
    if (!newUserCompanyId) return;
    setError(null);
    services.companies
      .createPlatformCompanyManager({
        companyId: Number(newUserCompanyId),
        email: newUserEmail,
        name: newUserName,
        password: newUserPassword,
      })
      .then((resp: UserSummary) => {
        const companyName =
          companies.find((c) => c.id === newUserCompanyId)?.name || 'Unknown';
        const newUser: CompanyUser = {
          id: String(resp.id),
          companyId: newUserCompanyId,
          companyName,
          email: resp.email,
          name: resp.name,
          role: resp.role === 'COMPANY_MANAGER' ? 'company_manager' : 'back_office_operator',
          enabled: resp.enabled,
          createdAt: new Date().toISOString(),
        };
        setUsers((prev) => [newUser, ...prev]);
        setIsCreateDialogOpen(false);
        setNewUserEmail('');
        setNewUserName('');
        setNewUserCompanyId('');
        setNewUserPassword('');
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to create user.');
      });
  };

  const handleToggleUser = (userId: string) => {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === userId ? { ...u, enabled: !u.enabled } : u
      )
    );
  };

  const getInitials = (name: string) =>
    name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);

  const getRoleLabel = (role: UserRole) => {
    switch (role) {
      case 'company_manager':
        return 'Manager';
      case 'back_office_operator':
        return 'Operator';
      case 'platform_admin':
        return 'Admin';
      default:
        return role;
    }
  };

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    setError(null);
    Promise.all([
      services.companies.listCompanies(),
      services.users.listPlatformUsers(),
    ])
      .then(([companyResp, userResp]: [CompanySummaryResponse[], UserSummary[]]) => {
        if (!active) return;
        setCompanies(
          companyResp.map((c) => ({
            id: String(c.id),
            name: c.name,
            vatNumber: c.vatNumber ?? '',
            createdAt: c.createdAt ?? undefined,
            enabled: true,
            userCount: 0,
          }))
        );
        setUsers(
          userResp.map((u) => ({
            id: String(u.id),
            companyId: 'unknown',
            companyName: 'Company',
            email: u.email,
            name: u.name,
            role: u.role === 'COMPANY_MANAGER' ? 'company_manager' : 'back_office_operator',
            enabled: u.enabled,
            createdAt: new Date().toISOString(),
          }))
        );
      })
      .catch((err: unknown) => {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Failed to load users.');
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
    <div className="flex-1 overflow-x-hidden">
      <TopBar title="User Provisioning" />

      <div className="p-4 lg:p-6 space-y-6">
        {error && (
          <div className="rounded-md border border-destructive/50 bg-destructive/5 px-3 py-2 text-sm text-destructive">
            {error}
          </div>
        )}

        
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold">User Provisioning</h2>
            <p className="text-muted-foreground">
              Create and manage users for registered companies.
            </p>
          </div>
          <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                Create User
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Company User</DialogTitle>
                <DialogDescription>
                  Provision a new user for an existing company.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="cu-company">Company</Label>
                  <Select
                    value={newUserCompanyId}
                    onValueChange={setNewUserCompanyId}
                  >
                    <SelectTrigger id="cu-company">
                      <SelectValue placeholder="Select a company" />
                    </SelectTrigger>
                    <SelectContent>
                      {enabledCompanies.map((c) => (
                        <SelectItem key={c.id} value={c.id}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cu-name">Full Name</Label>
                  <Input
                    id="cu-name"
                    placeholder="John Doe"
                    value={newUserName}
                    onChange={(e) => setNewUserName(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cu-email">Email</Label>
                  <Input
                    id="cu-email"
                    type="email"
                    placeholder="user@company.com"
                    value={newUserEmail}
                    onChange={(e) => setNewUserEmail(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cu-email">Password</Label>
                  <Input
                    id="cu-password"
                    type="text"
                    placeholder="Temporary Password"
                    value={newUserEmail}
                    onChange={(e) => setNewUserEmail(e.target.value)}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button
                  variant="outline"
                  onClick={() => setIsCreateDialogOpen(false)}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleCreateUser}
                  disabled={!newUserEmail || !newUserName || !newUserCompanyId}
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Create User
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Users</p>
                  <p className="text-2xl font-bold mt-1">{users.length}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <Users className="w-6 h-6 text-primary" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Managers</p>
                  <p className="text-2xl font-bold mt-1">
                    {users.filter((u) => u.role === 'company_manager').length}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-amber-500/10 flex items-center justify-center">
                  <Shield className="w-6 h-6 text-amber-500" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Operators</p>
                  <p className="text-2xl font-bold mt-1">
                    {users.filter((u) => u.role === 'back_office_operator').length}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-sky-500/10 flex items-center justify-center">
                  <UserCog className="w-6 h-6 text-sky-500" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search users..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
          <Select value={companyFilter} onValueChange={setCompanyFilter}>
            <SelectTrigger className="w-full sm:w-56">
              <Building2 className="w-4 h-4 mr-2" />
              <SelectValue placeholder="All companies" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Companies</SelectItem>
              {companies.map((c) => (
                <SelectItem key={c.id} value={c.id}>
                  {c.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        
        <Card>
          <CardHeader>
            <CardTitle>Company Users</CardTitle>
            <CardDescription>
              {filteredUsers.length} user{filteredUsers.length !== 1 ? 's' : ''} found
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {isLoading ? (
                <div className="text-center py-12 text-muted-foreground">Loading users…</div>
              ) : (
                filteredUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex flex-col gap-3 rounded-lg border p-4 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="flex min-w-0 items-center gap-4">
                    <Avatar className="h-10 w-10">
                      <AvatarFallback className="bg-primary/10 text-primary text-sm">
                        {getInitials(user.name)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="flex flex-wrap items-center gap-2">
                        <p className="font-medium">{user.name}</p>
                        <Badge
                          variant={user.enabled ? 'default' : 'destructive'}
                          className="text-xs"
                        >
                          {user.enabled ? 'Active' : 'Disabled'}
                        </Badge>
                      </div>
                      <div className="flex flex-wrap gap-x-3 gap-y-1 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1 break-all">
                          <Mail className="w-3 h-3" />
                          {user.email}
                        </span>
                        <span className="flex items-center gap-1 break-words">
                          <Building2 className="w-3 h-3" />
                          {user.companyName}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center gap-3 sm:shrink-0">
                    <Badge variant="outline" className="hidden sm:inline-flex capitalize">
                      {getRoleLabel(user.role)}
                    </Badge>
                    <span className="hidden lg:inline text-xs text-muted-foreground">
                      Created {formatDate(user.createdAt)}
                    </span>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="w-4 h-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => handleToggleUser(user.id)}
                        >
                          {user.enabled ? (
                            <>
                              <UserX className="w-4 h-4 mr-2" />
                              Disable User
                            </>
                          ) : (
                            <>
                              <Users className="w-4 h-4 mr-2" />
                              Enable User
                            </>
                          )}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem className="text-destructive">
                          <UserX className="w-4 h-4 mr-2" />
                          Remove User
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))
              )}

              {filteredUsers.length === 0 && (
                <div className="text-center py-12 text-muted-foreground">
                  No users found matching your search.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
