import type {
  User,
  Company,
  Client,
  Quote,
  Invoice,
  LineItem,
  QuoteVersion,
  CompanySummary,
  CompanyUser,
  PlatformMonitoringStats,
} from './types';

const generateId = () => Math.random().toString(36).substring(2, 11);

export const mockCompany: Company = {
  id: 'comp_1',
  name: 'Acme Solutions Ltd',
  email: 'contact@acmesolutions.com',
  phone: '+1 (555) 123-4567',
  address: '123 Business Avenue, Suite 100',
  city: 'San Francisco',
  country: 'United States',
  vatNumber: 'US123456789',
  createdAt: '2024-01-15T10:00:00Z',
};

export const mockUsers: User[] = [
  {
    id: 'user_1',
    email: 'admin@acmesolutions.com',
    name: 'Sarah Johnson',
    role: 'platform_admin',
    companyId: 'comp_1',
    assignedClientIds: [],
    avatar: undefined,
    createdAt: '2024-01-15T10:00:00Z',
  },
  {
    id: 'user_2',
    email: 'manager@acmesolutions.com',
    name: 'Michael Chen',
    role: 'company_manager',
    companyId: 'comp_1',
    assignedClientIds: [],
    avatar: undefined,
    createdAt: '2024-02-01T09:00:00Z',
  },
  {
    id: 'user_3',
    email: 'operator@acmesolutions.com',
    name: 'Emily Davis',
    role: 'back_office_operator',
    companyId: 'comp_1',
    assignedClientIds: ['client_1', 'client_2', 'client_3'],
    avatar: undefined,
    createdAt: '2024-03-10T14:00:00Z',
  },
];

export const mockClients: Client[] = [
  {
    id: 'client_1',
    companyId: 'comp_1',
    name: 'TechStart Inc.',
    email: 'billing@techstart.io',
    phone: '+1 (555) 234-5678',
    address: '456 Innovation Drive',
    city: 'Austin',
    country: 'United States',
    vatNumber: 'US987654321',
    notes: 'Preferred payment method: Bank transfer',
    createdAt: '2024-02-20T11:00:00Z',
  },
  {
    id: 'client_2',
    companyId: 'comp_1',
    name: 'Green Energy Co.',
    email: 'accounts@greenenergy.com',
    phone: '+1 (555) 345-6789',
    address: '789 Sustainable Way',
    city: 'Portland',
    country: 'United States',
    vatNumber: 'US456789123',
    createdAt: '2024-03-05T08:30:00Z',
  },
  {
    id: 'client_3',
    companyId: 'comp_1',
    name: 'Digital Marketing Pro',
    email: 'finance@dmpro.agency',
    phone: '+1 (555) 456-7890',
    address: '321 Creative Blvd',
    city: 'New York',
    country: 'United States',
    createdAt: '2024-03-15T16:00:00Z',
  },
  {
    id: 'client_4',
    companyId: 'comp_1',
    name: 'CloudServe Solutions',
    email: 'billing@cloudserve.net',
    phone: '+1 (555) 567-8901',
    address: '555 Cloud Computing Lane',
    city: 'Seattle',
    country: 'United States',
    vatNumber: 'US789123456',
    createdAt: '2024-04-01T10:00:00Z',
  },
  {
    id: 'client_5',
    companyId: 'comp_1',
    name: 'Retail Plus',
    email: 'accounts@retailplus.com',
    phone: '+1 (555) 678-9012',
    address: '888 Commerce Street',
    city: 'Chicago',
    country: 'United States',
    createdAt: '2024-04-10T12:00:00Z',
  },
];

const createLineItems = (items: Partial<LineItem>[]): LineItem[] => {
  return items.map((item, index) => ({
    id: `item_${generateId()}`,
    description: item.description || `Service ${index + 1}`,
    quantity: item.quantity || 1,
    unitPrice: item.unitPrice || 100,
    vatRate: item.vatRate || 20,
    discount: item.discount || 0,
    total: ((item.quantity || 1) * (item.unitPrice || 100) * (1 - (item.discount || 0) / 100)),
  }));
};

const createQuoteVersion = (
  quoteId: string,
  version: number,
  lineItems: LineItem[],
  notes: string,
  createdAt: string
): QuoteVersion => {
  const subtotal = lineItems.reduce((sum, item) => sum + item.total, 0);
  const vatTotal = lineItems.reduce((sum, item) => sum + (item.total * item.vatRate / 100), 0);
  const discountTotal = lineItems.reduce((sum, item) => {
    const gross = item.quantity * item.unitPrice;
    return sum + (gross * item.discount / 100);
  }, 0);

  return {
    id: `qv_${generateId()}`,
    quoteId,
    version,
    lineItems,
    subtotal,
    vatTotal,
    discountTotal,
    total: subtotal + vatTotal,
    notes,
    createdAt,
    createdBy: 'user_3',
  };
};

export const mockQuotes: Quote[] = [
  {
    id: 'quote_1',
    number: 'QUO-2024-001',
    companyId: 'comp_1',
    clientId: 'client_1',
    status: 'accepted',
    currentVersion: 2,
    versions: [
      createQuoteVersion('quote_1', 1, createLineItems([
        { description: 'Website Design & Development', quantity: 1, unitPrice: 5000 },
        { description: 'SEO Optimization Package', quantity: 1, unitPrice: 1500 },
      ]), 'Initial quote for web project', '2024-06-01T10:00:00Z'),
      createQuoteVersion('quote_1', 2, createLineItems([
        { description: 'Website Design & Development', quantity: 1, unitPrice: 5000 },
        { description: 'SEO Optimization Package', quantity: 1, unitPrice: 1500 },
        { description: 'Monthly Maintenance (3 months)', quantity: 3, unitPrice: 500 },
      ]), 'Added maintenance package per client request', '2024-06-05T14:00:00Z'),
    ],
    validUntil: '2024-07-01T23:59:59Z',
    createdAt: '2024-06-01T10:00:00Z',
    updatedAt: '2024-06-05T14:00:00Z',
    createdBy: 'user_3',
  },
  {
    id: 'quote_2',
    number: 'QUO-2024-002',
    companyId: 'comp_1',
    clientId: 'client_2',
    status: 'sent',
    currentVersion: 1,
    versions: [
      createQuoteVersion('quote_2', 1, createLineItems([
        { description: 'Energy Audit Consultation', quantity: 2, unitPrice: 800 },
        { description: 'Sustainability Report', quantity: 1, unitPrice: 2500 },
      ]), 'Environmental consulting services', '2024-06-10T09:00:00Z'),
    ],
    validUntil: '2024-07-10T23:59:59Z',
    createdAt: '2024-06-10T09:00:00Z',
    updatedAt: '2024-06-10T09:00:00Z',
    createdBy: 'user_3',
  },
  {
    id: 'quote_3',
    number: 'QUO-2024-003',
    companyId: 'comp_1',
    clientId: 'client_3',
    status: 'draft',
    currentVersion: 1,
    versions: [
      createQuoteVersion('quote_3', 1, createLineItems([
        { description: 'Brand Strategy Workshop', quantity: 1, unitPrice: 3000 },
        { description: 'Logo Design Package', quantity: 1, unitPrice: 1800 },
        { description: 'Brand Guidelines Document', quantity: 1, unitPrice: 1200 },
      ]), 'Complete branding package', '2024-06-15T11:00:00Z'),
    ],
    validUntil: '2024-07-15T23:59:59Z',
    createdAt: '2024-06-15T11:00:00Z',
    updatedAt: '2024-06-15T11:00:00Z',
    createdBy: 'user_3',
  },
  {
    id: 'quote_4',
    number: 'QUO-2024-004',
    companyId: 'comp_1',
    clientId: 'client_4',
    status: 'rejected',
    currentVersion: 1,
    versions: [
      createQuoteVersion('quote_4', 1, createLineItems([
        { description: 'Cloud Migration Services', quantity: 1, unitPrice: 15000 },
        { description: 'Staff Training (5 sessions)', quantity: 5, unitPrice: 500 },
      ]), 'Enterprise cloud migration', '2024-05-20T10:00:00Z'),
    ],
    validUntil: '2024-06-20T23:59:59Z',
    createdAt: '2024-05-20T10:00:00Z',
    updatedAt: '2024-05-25T16:00:00Z',
    createdBy: 'user_2',
  },
  {
    id: 'quote_5',
    number: 'QUO-2024-005',
    companyId: 'comp_1',
    clientId: 'client_5',
    status: 'expired',
    currentVersion: 1,
    versions: [
      createQuoteVersion('quote_5', 1, createLineItems([
        { description: 'POS System Integration', quantity: 1, unitPrice: 4000 },
        { description: 'Inventory Management Setup', quantity: 1, unitPrice: 2000 },
      ]), 'Retail system integration', '2024-04-01T10:00:00Z'),
    ],
    validUntil: '2024-05-01T23:59:59Z',
    createdAt: '2024-04-01T10:00:00Z',
    updatedAt: '2024-04-01T10:00:00Z',
    createdBy: 'user_3',
  },
];

const createInvoice = (
  id: string,
  number: string,
  clientId: string,
  status: Invoice['status'],
  lineItems: LineItem[],
  notes: string,
  dueDate: string,
  createdAt: string,
  quoteId?: string,
  paidDate?: string
): Invoice => {
  const subtotal = lineItems.reduce((sum, item) => sum + item.total, 0);
  const vatTotal = lineItems.reduce((sum, item) => sum + (item.total * item.vatRate / 100), 0);
  const discountTotal = lineItems.reduce((sum, item) => {
    const gross = item.quantity * item.unitPrice;
    return sum + (gross * item.discount / 100);
  }, 0);

  return {
    id,
    number,
    companyId: 'comp_1',
    clientId,
    quoteId,
    status,
    lineItems,
    subtotal,
    vatTotal,
    discountTotal,
    total: subtotal + vatTotal,
    notes,
    dueDate,
    paidDate,
    createdAt,
    updatedAt: createdAt,
    createdBy: 'user_3',
  };
};

export const mockInvoices: Invoice[] = [
  createInvoice(
    'inv_1',
    'INV-2024-001',
    'client_1',
    'paid',
    createLineItems([
      { description: 'Website Design & Development', quantity: 1, unitPrice: 5000 },
      { description: 'SEO Optimization Package', quantity: 1, unitPrice: 1500 },
      { description: 'Monthly Maintenance (3 months)', quantity: 3, unitPrice: 500 },
    ]),
    'Thank you for your business!',
    '2024-07-15T23:59:59Z',
    '2024-06-15T10:00:00Z',
    'quote_1',
    '2024-07-10T14:30:00Z'
  ),
  createInvoice(
    'inv_2',
    'INV-2024-002',
    'client_2',
    'sent',
    createLineItems([
      { description: 'Initial Consultation', quantity: 2, unitPrice: 300 },
      { description: 'Project Planning Phase', quantity: 1, unitPrice: 1200 },
    ]),
    'Payment due within 30 days',
    '2024-07-20T23:59:59Z',
    '2024-06-20T11:00:00Z'
  ),
  createInvoice(
    'inv_3',
    'INV-2024-003',
    'client_3',
    'overdue',
    createLineItems([
      { description: 'Marketing Campaign Setup', quantity: 1, unitPrice: 2500 },
      { description: 'Social Media Management (1 month)', quantity: 1, unitPrice: 800 },
    ]),
    'Please process payment at your earliest convenience',
    '2024-06-01T23:59:59Z',
    '2024-05-01T09:00:00Z'
  ),
  createInvoice(
    'inv_4',
    'INV-2024-004',
    'client_4',
    'draft',
    createLineItems([
      { description: 'Technical Support (10 hours)', quantity: 10, unitPrice: 150 },
      { description: 'System Updates', quantity: 1, unitPrice: 500 },
    ]),
    'Draft invoice for review',
    '2024-08-01T23:59:59Z',
    '2024-07-01T10:00:00Z'
  ),
  createInvoice(
    'inv_5',
    'INV-2024-005',
    'client_1',
    'paid',
    createLineItems([
      { description: 'Quarterly Retainer', quantity: 1, unitPrice: 3000 },
    ]),
    'Q2 2024 Retainer',
    '2024-04-30T23:59:59Z',
    '2024-04-01T10:00:00Z',
    undefined,
    '2024-04-25T11:00:00Z'
  ),
  createInvoice(
    'inv_6',
    'INV-2024-006',
    'client_5',
    'sent',
    createLineItems([
      { description: 'E-commerce Platform Development', quantity: 1, unitPrice: 8000 },
      { description: 'Payment Gateway Integration', quantity: 1, unitPrice: 1500 },
      { description: 'Training Sessions', quantity: 4, unitPrice: 250, discount: 10 },
    ]),
    'Net 30 payment terms',
    '2024-07-30T23:59:59Z',
    '2024-06-30T14:00:00Z'
  ),
];

export const calculateDashboardStats = (quotes: Quote[], invoices: Invoice[]) => {
  const totalQuotes = quotes.length;
  const pendingQuotes = quotes.filter(q => q.status === 'sent' || q.status === 'draft').length;
  const totalInvoices = invoices.length;
  const unpaidInvoices = invoices.filter(i => i.status === 'sent' || i.status === 'overdue').length;
  const totalRevenue = invoices
    .filter(i => i.status === 'paid')
    .reduce((sum, i) => sum + i.total, 0);
  const monthlyRevenue = invoices
    .filter(i => {
      const invoiceDate = new Date(i.createdAt);
      const now = new Date();
      return invoiceDate.getMonth() === now.getMonth() && 
             invoiceDate.getFullYear() === now.getFullYear() &&
             i.status === 'paid';
    })
    .reduce((sum, i) => sum + i.total, 0);

  return {
    totalQuotes,
    pendingQuotes,
    totalInvoices,
    unpaidInvoices,
    totalRevenue,
    monthlyRevenue,
  };
};

export interface ActivityItem {
  id: string;
  type: 'quote_created' | 'quote_sent' | 'quote_accepted' | 'invoice_created' | 'invoice_paid' | 'client_added';
  description: string;
  timestamp: string;
  entityId: string;
  entityType: 'quote' | 'invoice' | 'client';
}

export const mockActivity: ActivityItem[] = [
  {
    id: 'act_1',
    type: 'invoice_paid',
    description: 'Invoice INV-2024-001 was paid by TechStart Inc.',
    timestamp: '2024-07-10T14:30:00Z',
    entityId: 'inv_1',
    entityType: 'invoice',
  },
  {
    id: 'act_2',
    type: 'quote_accepted',
    description: 'Quote QUO-2024-001 was accepted by TechStart Inc.',
    timestamp: '2024-06-08T11:00:00Z',
    entityId: 'quote_1',
    entityType: 'quote',
  },
  {
    id: 'act_3',
    type: 'invoice_created',
    description: 'Invoice INV-2024-006 was created for Retail Plus',
    timestamp: '2024-06-30T14:00:00Z',
    entityId: 'inv_6',
    entityType: 'invoice',
  },
  {
    id: 'act_4',
    type: 'quote_sent',
    description: 'Quote QUO-2024-002 was sent to Green Energy Co.',
    timestamp: '2024-06-10T09:30:00Z',
    entityId: 'quote_2',
    entityType: 'quote',
  },
  {
    id: 'act_5',
    type: 'client_added',
    description: 'New client Retail Plus was added',
    timestamp: '2024-04-10T12:00:00Z',
    entityId: 'client_5',
    entityType: 'client',
  },
];


/** Mock companies as seen by platform admin */
export const mockCompanySummaries: CompanySummary[] = [
  {
    id: 'comp_1',
    name: 'Acme Solutions Ltd',
    vatNumber: 'US123456789',
    email: 'contact@acmesolutions.com',
    enabled: true,
    createdAt: '2024-01-15T10:00:00Z',
    userCount: 3,
  },
  {
    id: 'comp_2',
    name: 'BrightWave Digital',
    vatNumber: 'US234567890',
    email: 'info@brightwavedigital.com',
    enabled: true,
    createdAt: '2024-02-20T08:00:00Z',
    userCount: 5,
  },
  {
    id: 'comp_3',
    name: 'Nova Consulting Group',
    vatNumber: 'US345678901',
    email: 'admin@novaconsulting.com',
    enabled: false,
    createdAt: '2024-03-10T14:30:00Z',
    userCount: 2,
  },
  {
    id: 'comp_4',
    name: 'Pinnacle Services Inc.',
    vatNumber: 'US456789012',
    email: 'hello@pinnacleservices.com',
    enabled: true,
    createdAt: '2024-04-05T11:00:00Z',
    userCount: 4,
  },
  {
    id: 'comp_5',
    name: 'Redline Logistics',
    vatNumber: 'US567890123',
    email: 'ops@redlinelogistics.com',
    enabled: true,
    createdAt: '2024-05-18T09:00:00Z',
    userCount: 1,
  },
];

/** Mock company users for provisioning view */
export const mockCompanyUsers: CompanyUser[] = [
  {
    id: 'cu_1',
    companyId: 'comp_1',
    companyName: 'Acme Solutions Ltd',
    email: 'manager@acmesolutions.com',
    name: 'Michael Chen',
    role: 'company_manager',
    enabled: true,
    createdAt: '2024-02-01T09:00:00Z',
  },
  {
    id: 'cu_2',
    companyId: 'comp_1',
    companyName: 'Acme Solutions Ltd',
    email: 'operator@acmesolutions.com',
    name: 'Emily Davis',
    role: 'back_office_operator',
    enabled: true,
    createdAt: '2024-03-10T14:00:00Z',
  },
  {
    id: 'cu_3',
    companyId: 'comp_2',
    companyName: 'BrightWave Digital',
    email: 'lisa@brightwavedigital.com',
    name: 'Lisa Park',
    role: 'company_manager',
    enabled: true,
    createdAt: '2024-02-25T10:00:00Z',
  },
  {
    id: 'cu_4',
    companyId: 'comp_2',
    companyName: 'BrightWave Digital',
    email: 'james@brightwavedigital.com',
    name: 'James Wright',
    role: 'back_office_operator',
    enabled: true,
    createdAt: '2024-03-15T12:00:00Z',
  },
  {
    id: 'cu_5',
    companyId: 'comp_3',
    companyName: 'Nova Consulting Group',
    email: 'tom@novaconsulting.com',
    name: 'Tom Rivera',
    role: 'company_manager',
    enabled: false,
    createdAt: '2024-03-12T08:00:00Z',
  },
  {
    id: 'cu_6',
    companyId: 'comp_4',
    companyName: 'Pinnacle Services Inc.',
    email: 'anna@pinnacleservices.com',
    name: 'Anna Schmidt',
    role: 'company_manager',
    enabled: true,
    createdAt: '2024-04-10T11:00:00Z',
  },
  {
    id: 'cu_7',
    companyId: 'comp_5',
    companyName: 'Redline Logistics',
    email: 'mark@redlinelogistics.com',
    name: 'Mark Thompson',
    role: 'company_manager',
    enabled: true,
    createdAt: '2024-05-20T09:00:00Z',
  },
];

/** Mock platform monitoring stats */
export const mockPlatformMonitoring: PlatformMonitoringStats = {
  totalPdfCount: 1_247,
  totalDiskUsageBytes: 3_412_582_400, // ~3.18 GB
  companyBreakdown: [
    {
      companyId: 'comp_1',
      companyName: 'Acme Solutions Ltd',
      pdfCount: 412,
      diskUsageBytes: 1_125_376_000, // ~1.05 GB
    },
    {
      companyId: 'comp_2',
      companyName: 'BrightWave Digital',
      pdfCount: 389,
      diskUsageBytes: 987_654_000, // ~941 MB
    },
    {
      companyId: 'comp_3',
      companyName: 'Nova Consulting Group',
      pdfCount: 87,
      diskUsageBytes: 234_567_000, // ~224 MB
    },
    {
      companyId: 'comp_4',
      companyName: 'Pinnacle Services Inc.',
      pdfCount: 298,
      diskUsageBytes: 876_543_000, // ~836 MB
    },
    {
      companyId: 'comp_5',
      companyName: 'Redline Logistics',
      pdfCount: 61,
      diskUsageBytes: 188_442_400, // ~180 MB
    },
  ],
};
