'use client';

import { Link } from 'react-router-dom';
import {
  Building2,
  Users,
  BarChart3,
  ArrowRight,
  FileText,
  HardDrive,
  ToggleRight,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/lib/auth-context';
import {
  mockCompanySummaries,
  mockCompanyUsers,
  mockPlatformMonitoring,
} from '@/lib/mock-data';

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export function AdminDashboardPage() {
  const { user } = useAuth();

  const activeCompanies = mockCompanySummaries.filter((c) => c.enabled).length;
  const totalUsers = mockCompanyUsers.length;

  return (
    <div className="flex-1">
      <TopBar title="Platform Admin" />

      <div className="p-4 lg:p-6 space-y-6">
        
        <div>
          <h2 className="text-2xl font-bold">
            Welcome back, {user?.name.split(' ')[0]}
          </h2>
          <p className="text-muted-foreground">
            Platform administration overview.
          </p>
        </div>

        
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Companies</p>
                  <p className="text-2xl font-bold mt-1">
                    {mockCompanySummaries.length}
                  </p>
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
                  <p className="text-2xl font-bold mt-1">{activeCompanies}</p>
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
                  <p className="text-sm text-muted-foreground">Users</p>
                  <p className="text-2xl font-bold mt-1">{totalUsers}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-sky-500/10 flex items-center justify-center">
                  <Users className="w-6 h-6 text-sky-500" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total PDFs</p>
                  <p className="text-2xl font-bold mt-1">
                    {mockPlatformMonitoring.totalPdfCount.toLocaleString()}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-amber-500/10 flex items-center justify-center">
                  <FileText className="w-6 h-6 text-amber-500" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-base font-semibold">Storage Overview</CardTitle>
            <Button asChild variant="ghost" size="sm">
              <Link to="/admin/monitoring">
                View details
                <ArrowRight className="w-4 h-4 ml-1" />
              </Link>
            </Button>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-4">
              <div className="h-16 w-16 rounded-xl bg-amber-500/10 flex items-center justify-center">
                <HardDrive className="w-8 h-8 text-amber-500" />
              </div>
              <div>
                <p className="text-3xl font-bold">
                  {formatBytes(mockPlatformMonitoring.totalDiskUsageBytes)}
                </p>
                <p className="text-sm text-muted-foreground">
                  Total storage across {mockPlatformMonitoring.companyBreakdown.length} companies
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        
        <Card>
          <CardHeader>
            <CardTitle className="text-base font-semibold">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <Button
                asChild
                variant="outline"
                className="h-auto py-4 flex-col gap-2 bg-transparent"
              >
                <Link to="/admin/companies">
                  <Building2 className="w-5 h-5" />
                  <span>Manage Companies</span>
                </Link>
              </Button>
              <Button
                asChild
                variant="outline"
                className="h-auto py-4 flex-col gap-2 bg-transparent"
              >
                <Link to="/admin/users">
                  <Users className="w-5 h-5" />
                  <span>Provision Users</span>
                </Link>
              </Button>
              <Button
                asChild
                variant="outline"
                className="h-auto py-4 flex-col gap-2 bg-transparent"
              >
                <Link to="/admin/monitoring">
                  <BarChart3 className="w-5 h-5" />
                  <span>View Monitoring</span>
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
