export type UserRole = 'platform_admin' | 'company_manager' | 'back_office_operator';

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  companyId: string;
  companyName?: string;
  assignedClientIds: string[];
  avatar?: string;
  createdAt: string;
}

export interface Company {
  id: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  country: string;
  vatNumber: string;
  logo?: string;
  createdAt: string;
}


/** High-level company summary visible to platform admins */
export interface CompanySummary {
  id: string;
  name: string;
  vatNumber: string;
  email?: string;
  enabled?: boolean;
  createdAt?: string;
  userCount?: number;
}

/** User record as seen by platform admin during provisioning */
export interface CompanyUser {
  id: string;
  companyId: string;
  companyName: string;
  email: string;
  name: string;
  role: UserRole;
  enabled: boolean;
  password?: string;
  createdAt: string;
}

/** Platform-wide monitoring stats for PDF storage */
export interface PlatformMonitoringStats {
  totalPdfCount: number;
  totalDiskUsageBytes: number;
  companyBreakdown: CompanyStorageStats[];
}

/** Per-company storage breakdown */
export interface CompanyStorageStats {
  companyId: string;
  companyName: string;
  pdfCount: number;
  diskUsageBytes: number;
}

export interface Client {
  id: string;
  companyId: string;
  name: string;
  displayName?: string;
  legalName?: string;
  contactName?: string;
  companyName?: string;
  email: string;
  phone: string;
  pec?: string;
  address: string;
  city: string;
  postalCode?: string;
  country: string;
  vatNumber?: string;
  status?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
}

export type QuoteStatus = 'draft' | 'sent' | 'accepted' | 'rejected' | 'expired' | 'converted' | 'archived';
export type InvoiceStatus = 'draft' | 'issued' | 'paid' | 'overdue' | 'archived' | 'sent' | 'cancelled';

export interface LineItem {
  id: string;
  description: string;
  quantity: number;
  unit?: string;
  unitPrice: number;
  vatRate: number;
  taxRate?: number;
  discount: number;
  discountType?: string;
  discountValue?: number;
  notes?: string;
  lineSubtotalAmount?: number;
  lineTaxAmount?: number;
  lineTotalAmount?: number;
  total: number;
}

export interface QuoteVersion {
  id: string;
  quoteId: string;
  version: number;
  lineItems: LineItem[];
  subtotal: number;
  vatTotal: number;
  discountTotal: number;
  total: number;
  notes: string;
  createdAt: string;
  createdBy: string;
}

export interface Quote {
  id: string;
  number: string;
  quoteNumber?: string | number;
  quoteYear?: number;
  companyId: string;
  clientId: string;
  status: QuoteStatus;
  title?: string;
  currentVersion: number;
  versions: QuoteVersion[];
  items?: LineItem[];
  notes?: string;
  issueDate?: string;
  validUntil: string;
  currency?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface Invoice {
  id: string;
  number: string;
  invoiceNumber?: string | number;
  invoiceYear?: number;
  title?: string;
  companyId: string;
  clientId: string;
  quoteId?: string;
  linkedQuoteId?: string;
  status: InvoiceStatus;
  items?: LineItem[];
  lineItems: LineItem[];
  subtotal: number;
  vatTotal: number;
  taxRate?: number;
  taxAmount?: number;
  discountTotal: number;
  total: number;
  notes: string;
  terms?: string;
  issueDate?: string;
  dueDate: string;
  paidDate?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string, companyId?: number | null) => Promise<boolean>;
  logout: () => void;
  switchRole: (role: UserRole) => void;
}

export interface DashboardStats {
  totalQuotes: number;
  pendingQuotes: number;
  totalInvoices: number;
  unpaidInvoices: number;
  totalRevenue: number;
  monthlyRevenue: number;
}
