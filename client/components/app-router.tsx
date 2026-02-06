"use client"

import React, { Suspense, useEffect, useState } from "react"
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import { AuthProvider, useAuth, usePermissions } from "@/lib/auth-context"
import { AppShell } from "@/components/layout/app-shell"



const LoginPage = React.lazy(() =>
  import("@/components/pages/login-page").then((m) => ({ default: m.LoginPage }))
)

const DashboardPage = React.lazy(() =>
  import("@/components/pages/dashboard-page").then((m) => ({ default: m.DashboardPage }))
)
const ClientsListPage = React.lazy(() =>
  import("@/components/pages/clients-list-page").then((m) => ({ default: m.ClientsListPage }))
)
const ClientDetailPage = React.lazy(() =>
  import("@/components/pages/client-detail-page").then((m) => ({ default: m.ClientDetailPage }))
)
const ClientEditorPage = React.lazy(() =>
  import("@/components/pages/client-editor-page").then((m) => ({ default: m.ClientEditorPage }))
)
const QuotesListPage = React.lazy(() =>
  import("@/components/pages/quotes-list-page").then((m) => ({ default: m.QuotesListPage }))
)
const QuoteEditorPage = React.lazy(() =>
  import("@/components/pages/quote-editor-page").then((m) => ({ default: m.QuoteEditorPage }))
)
const QuoteDetailPage = React.lazy(() =>
  import("@/components/pages/quote-detail-page").then((m) => ({ default: m.QuoteDetailPage }))
)
const InvoicesListPage = React.lazy(() =>
  import("@/components/pages/invoices-list-page").then((m) => ({ default: m.InvoicesListPage }))
)
const InvoiceEditorPage = React.lazy(() =>
  import("@/components/pages/invoice-editor-page").then((m) => ({ default: m.InvoiceEditorPage }))
)
const InvoiceDetailPage = React.lazy(() =>
  import("@/components/pages/invoice-detail-page").then((m) => ({ default: m.InvoiceDetailPage }))
)
const PdfPreviewPage = React.lazy(() =>
  import("@/components/pages/pdf-preview-page").then((m) => ({ default: m.PdfPreviewPage }))
)
const SettingsPage = React.lazy(() =>
  import("@/components/pages/settings-page").then((m) => ({ default: m.SettingsPage }))
)
const UsersPage = React.lazy(() =>
  import("@/components/pages/users-page").then((m) => ({ default: m.UsersPage }))
)

const AdminDashboardPage = React.lazy(() =>
  import("@/components/pages/admin-dashboard-page").then((m) => ({ default: m.AdminDashboardPage }))
)
const AdminCompaniesPage = React.lazy(() =>
  import("@/components/pages/admin-companies-page").then((m) => ({ default: m.AdminCompaniesPage }))
)
const AdminUsersPage = React.lazy(() =>
  import("@/components/pages/admin-users-page").then((m) => ({ default: m.AdminUsersPage }))
)
const AdminMonitoringPage = React.lazy(() =>
  import("@/components/pages/admin-monitoring-page").then((m) => ({ default: m.AdminMonitoringPage }))
)



function PageSpinner() {
  return (
    <div className="flex h-full min-h-[200px] items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
    </div>
  )
}



function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return <AppShell>{children}</AppShell>
}

function CompanyRoute({ children }: { children: React.ReactNode }) {
  const { canAccessCompanyData } = usePermissions()
  if (!canAccessCompanyData) return <Navigate to="/admin" replace />
  return <>{children}</>
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isPlatformAdmin } = usePermissions()
  if (!isPlatformAdmin) return <Navigate to="/" replace />
  return <>{children}</>
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    )
  }

  if (user) {
    return user.role === "platform_admin"
      ? <Navigate to="/admin" replace />
      : <Navigate to="/" replace />
  }

  return <>{children}</>
}

function HomeRedirect() {
  const { user } = useAuth()
  if (user?.role === "platform_admin") return <Navigate to="/admin" replace />
  return (
    <Suspense fallback={<PageSpinner />}>
      <DashboardPage />
    </Suspense>
  )
}



function AppRoutes() {
  return (
    <Routes>
      
      <Route
        path="/login"
        element={
          <PublicRoute>
            <Suspense fallback={<PageSpinner />}>
              <LoginPage />
            </Suspense>
          </PublicRoute>
        }
      />

      
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomeRedirect />
          </ProtectedRoute>
        }
      />

      
      <Route path="/clients" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><ClientsListPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/clients/new" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><ClientEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/clients/:id" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><ClientDetailPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/clients/:id/edit" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><ClientEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />

      <Route path="/quotes" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><QuotesListPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/quotes/new" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><QuoteEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/quotes/:id" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><QuoteDetailPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/quotes/:id/edit" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><QuoteEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/quotes/:id/preview" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><PdfPreviewPage /></Suspense></CompanyRoute></ProtectedRoute>} />

      <Route path="/invoices" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><InvoicesListPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/invoices/new" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><InvoiceEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/invoices/:id" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><InvoiceDetailPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/invoices/:id/edit" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><InvoiceEditorPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/invoices/:id/preview" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><PdfPreviewPage /></Suspense></CompanyRoute></ProtectedRoute>} />

      <Route path="/settings" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><SettingsPage /></Suspense></CompanyRoute></ProtectedRoute>} />
      <Route path="/users" element={<ProtectedRoute><CompanyRoute><Suspense fallback={<PageSpinner />}><UsersPage /></Suspense></CompanyRoute></ProtectedRoute>} />

      
      <Route path="/admin" element={<ProtectedRoute><AdminRoute><Suspense fallback={<PageSpinner />}><AdminDashboardPage /></Suspense></AdminRoute></ProtectedRoute>} />
      <Route path="/admin/companies" element={<ProtectedRoute><AdminRoute><Suspense fallback={<PageSpinner />}><AdminCompaniesPage /></Suspense></AdminRoute></ProtectedRoute>} />
      <Route path="/admin/users" element={<ProtectedRoute><AdminRoute><Suspense fallback={<PageSpinner />}><AdminUsersPage /></Suspense></AdminRoute></ProtectedRoute>} />
      <Route path="/admin/monitoring" element={<ProtectedRoute><AdminRoute><Suspense fallback={<PageSpinner />}><AdminMonitoringPage /></Suspense></AdminRoute></ProtectedRoute>} />

      
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}



export function AppRouter() {
  const [isMounted, setIsMounted] = useState(false)

  useEffect(() => {
    setIsMounted(true)
  }, [])

  if (!isMounted) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    )
  }

  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
