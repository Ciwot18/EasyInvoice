'use client';

import {
  FileText,
  HardDrive,
  Building2,
  Database,
  BarChart3,
} from 'lucide-react';
import { TopBar } from '@/components/layout/top-bar';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { mockPlatformMonitoring } from '@/lib/mock-data';

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export function AdminMonitoringPage() {
  const stats = mockPlatformMonitoring;

  const maxPdfCount = Math.max(...stats.companyBreakdown.map((c) => c.pdfCount));
  const maxDiskUsage = Math.max(...stats.companyBreakdown.map((c) => c.diskUsageBytes));

  return (
    <div className="flex-1">
      <TopBar title="Platform Monitoring" />

      <div className="p-4 lg:p-6 space-y-6">
        
        <div>
          <h2 className="text-2xl font-bold">Platform Monitoring</h2>
          <p className="text-muted-foreground">
            Overview of PDF storage usage across the entire platform.
            This is a read-only monitoring view.
          </p>
        </div>

        
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total PDFs</p>
                  <p className="text-2xl font-bold mt-1">
                    {stats.totalPdfCount.toLocaleString()}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <FileText className="w-6 h-6 text-primary" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Disk Usage</p>
                  <p className="text-2xl font-bold mt-1">
                    {formatBytes(stats.totalDiskUsageBytes)}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-amber-500/10 flex items-center justify-center">
                  <HardDrive className="w-6 h-6 text-amber-500" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Companies</p>
                  <p className="text-2xl font-bold mt-1">
                    {stats.companyBreakdown.length}
                  </p>
                </div>
                <div className="h-12 w-12 rounded-full bg-emerald-500/10 flex items-center justify-center">
                  <Building2 className="w-6 h-6 text-emerald-500" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-muted-foreground" />
              <div>
                <CardTitle>Storage by Company</CardTitle>
                <CardDescription>
                  PDF count and disk usage breakdown per company.
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {stats.companyBreakdown
                .sort((a, b) => b.diskUsageBytes - a.diskUsageBytes)
                .map((company) => {
                  const pdfPercent = maxPdfCount > 0 ? (company.pdfCount / maxPdfCount) * 100 : 0;
                  const diskPercent = maxDiskUsage > 0 ? (company.diskUsageBytes / maxDiskUsage) * 100 : 0;
                  const sharePercent = stats.totalDiskUsageBytes > 0
                    ? ((company.diskUsageBytes / stats.totalDiskUsageBytes) * 100).toFixed(1)
                    : '0';

                  return (
                    <div key={company.companyId} className="space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="h-9 w-9 rounded-lg bg-muted flex items-center justify-center shrink-0">
                            <Building2 className="w-4 h-4 text-muted-foreground" />
                          </div>
                          <div>
                            <p className="font-medium text-sm">{company.companyName}</p>
                            <p className="text-xs text-muted-foreground">
                              {sharePercent}% of total storage
                            </p>
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pl-12">
                        <div className="space-y-1.5">
                          <div className="flex items-center justify-between text-sm">
                            <span className="flex items-center gap-1.5 text-muted-foreground">
                              <FileText className="w-3.5 h-3.5" />
                              PDF Files
                            </span>
                            <span className="font-medium">
                              {company.pdfCount.toLocaleString()}
                            </span>
                          </div>
                          <Progress value={pdfPercent} className="h-2" />
                        </div>
                        <div className="space-y-1.5">
                          <div className="flex items-center justify-between text-sm">
                            <span className="flex items-center gap-1.5 text-muted-foreground">
                              <Database className="w-3.5 h-3.5" />
                              Disk Usage
                            </span>
                            <span className="font-medium">
                              {formatBytes(company.diskUsageBytes)}
                            </span>
                          </div>
                          <Progress value={diskPercent} className="h-2" />
                        </div>
                      </div>
                    </div>
                  );
                })}
            </div>
          </CardContent>
        </Card>

        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-start gap-3">
              <div className="h-9 w-9 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                <Database className="w-4 h-4 text-primary" />
              </div>
              <div>
                <p className="font-medium text-sm">Monitoring Only</p>
                <p className="text-sm text-muted-foreground mt-0.5">
                  This dashboard shows aggregate PDF storage metrics.
                  No invoice or quote content is accessible from this view.
                  Data refreshes periodically from the backend storage service.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
