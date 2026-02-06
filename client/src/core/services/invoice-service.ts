import { BaseService } from "./base-service";
import { Page, PageQuery } from "@/src/core/models/common";
import type { CreateInvoiceItemRequest } from "./invoice-item-service";

/** Payload for creating an invoice. */
export interface CreateInvoiceRequest {
  /** Customer identifier. */
  customerId: number;
  /** Source quote id (optional). */
  sourceQuoteId?: number;
  /** Invoice title (optional). */
  title?: string;
  /** Notes (optional). */
  notes?: string;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string;
  /** Due date (YYYY-MM-DD). */
  dueDate?: string;
  /** Currency code (e.g., EUR). */
  currency?: string;
  /** Invoice items. */
  items: CreateInvoiceItemRequest[];
}

/** Payload for updating an invoice. */
export interface UpdateInvoiceRequest {
  /** Invoice title (optional). */
  title?: string;
  /** Notes (optional). */
  notes?: string;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string;
  /** Due date (YYYY-MM-DD). */
  dueDate?: string;
  /** Currency code (e.g., EUR). */
  currency?: string;
}

/** Summary response for an invoice. */
export interface InvoiceSummaryResponse {
  /** Invoice identifier. */
  id: number | string;
  /** Invoice year (e.g., 2024). */
  invoiceYear: number | null;
  /** Invoice progressive number within the year. */
  invoiceNumber: number | null;
  /** Invoice status (backend enum). */
  status: "DRAFT" | "ISSUED" | "PAID" | "OVERDUE" | "ARCHIVED" | string;
  /** Invoice title. */
  title?: string | null;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string | null;
  /** Due date (YYYY-MM-DD). */
  dueDate?: string | null;
  /** Currency code (e.g., EUR). */
  currency?: string | null;
  /** Total amount. */
  totalAmount?: number | null;
  /** Customer id. */
  customerId?: number | string | null;
  /** Customer display name. */
  customerDisplayName?: string | null;
}

/** Detailed response for an invoice. */
export interface InvoiceDetailResponse extends InvoiceSummaryResponse {
  /** Notes. */
  notes?: string | null;
  /** Subtotal amount. */
  subtotalAmount?: number | null;
  /** Tax amount. */
  taxAmount?: number | null;
  /** Total amount. */
  totalAmount?: number | null;
  /** Source quote id (if converted). */
  sourceQuoteId?: number | null;
  /** Created date-time. */
  createdAt?: string | null;
  /** Updated date-time. */
  updatedAt?: string | null;
}

/** Response when a PDF is saved/issued. */
export interface InvoicePdfArchiveSaveResponse {
  /** Saved PDF identifier. */
  saveId: number;
  /** Invoice identifier. */
  invoiceId: number;
  /** Created date-time. */
  createdAt: string;
}

/** Summary for stored PDF versions. */
export interface InvoicePdfDto {
  /** PDF identifier. */
  id: number;
  /** PDF file name. */
  fileName?: string | null;
  /** Created date-time. */
  createdAt?: string | null;
}

/** Invoice-related API service. */
export class InvoiceService extends BaseService {
  /** Base path for invoice endpoints. */
  protected basePath(): string {
    return "/invoices";
  }

  /**
   * Creates a new invoice.
   * @param request creation payload
   */
  createInvoice(request: CreateInvoiceRequest): Promise<InvoiceDetailResponse> {
    return this.post<InvoiceDetailResponse, CreateInvoiceRequest>(this.endpoint(), { body: request });
  }

  /**
   * Lists invoices with pagination.
   * @param params pagination and query params
   */
  listInvoices(params: PageQuery = {}): Promise<Page<InvoiceSummaryResponse>> {
    return this.get<Page<InvoiceSummaryResponse>>(this.endpoint(), { query: params });
  }

  /**
   * Returns an invoice by id.
   * @param invoiceId invoice identifier
   */
  getInvoice(invoiceId: number | string): Promise<InvoiceDetailResponse> {
    return this.get<InvoiceDetailResponse>(this.endpoint(`/${invoiceId}`));
  }

  /**
   * Updates an invoice by id.
   * @param invoiceId invoice identifier
   * @param request update payload
   */
  updateInvoice(invoiceId: number | string, request: UpdateInvoiceRequest): Promise<InvoiceDetailResponse> {
    return this.patch<InvoiceDetailResponse, UpdateInvoiceRequest>(this.endpoint(`/${invoiceId}`), { body: request });
  }

  /**
   * Issues an invoice and stores a PDF version.
   * @param invoiceId invoice identifier
   */
  issueInvoice(invoiceId: number | string): Promise<InvoicePdfArchiveSaveResponse> {
    return this.post<InvoicePdfArchiveSaveResponse, unknown>(this.endpoint(`/${invoiceId}/issue`));
  }

  /**
   * Marks an invoice as paid.
   * @param invoiceId invoice identifier
   */
  payInvoice(invoiceId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${invoiceId}/pay`));
  }

  /**
   * Marks an invoice as overdue.
   * @param invoiceId invoice identifier
   */
  markOverdue(invoiceId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${invoiceId}/overdue`));
  }

  /**
   * Returns the invoice PDF (inline).
   * @param invoiceId invoice identifier
   */
  getInvoicePdf(invoiceId: number | string): Promise<Blob> {
    return this.get<Blob>(this.endpoint(`/${invoiceId}/pdf`), { responseType: "blob" });
  }

  /**
   * Returns the invoice PDF as a download.
   * @param invoiceId invoice identifier
   */
  getInvoicePdfDownload(invoiceId: number | string): Promise<Blob> {
    return this.get<Blob>(this.endpoint(`/${invoiceId}/pdf-download`), { responseType: "blob" });
  }

  /**
   * Lists stored PDF versions for an invoice.
   * @param invoiceId invoice identifier
   */
  listPdfVersions(invoiceId: number | string): Promise<InvoicePdfDto[]> {
    return this.get<InvoicePdfDto[]>(this.endpoint(`/${invoiceId}/pdfs`));
  }

  /**
   * Downloads a stored PDF version for an invoice.
   * @param invoiceId invoice identifier
   * @param saveId stored PDF identifier
   */
  downloadPdfVersion(invoiceId: number | string, saveId: number | string): Promise<Blob> {
    return this.get<Blob>(this.endpoint(`/${invoiceId}/pdfs/${saveId}`), { responseType: "blob" });
  }
}
