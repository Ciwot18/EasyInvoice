import { BaseService } from "./base-service";
import { Page, PageQuery } from "@/src/core/models/common";
import type { CreateQuoteItemRequest } from "./quote-item-service";

/** Payload for creating a quote. */
export interface CreateQuoteRequest {
  /** Customer identifier. */
  customerId: number;
  /** Quote title (optional). */
  title?: string;
  /** Notes (optional). */
  notes?: string;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string;
  /** Valid until date (YYYY-MM-DD). */
  validUntil?: string;
  /** Currency code (e.g., EUR). */
  currency?: string;
  /** Quote items. */
  items: CreateQuoteItemRequest[];
}

/** Payload for updating a quote. */
export interface UpdateQuoteRequest {
  /** Quote title (optional). */
  title?: string;
  /** Notes (optional). */
  notes?: string;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string;
  /** Valid until date (YYYY-MM-DD). */
  validUntil?: string;
  /** Currency code (e.g., EUR). */
  currency?: string;
}

/** Summary response for a quote. */
export interface QuoteSummaryResponse {
  /** Quote identifier. */
  id: number | string;
  /** Quote year (e.g., 2024). */
  quoteYear: number | null;
  /** Quote progressive number within the year. */
  quoteNumber: number | null;
  /** Quote status (backend enum). */
  status: "DRAFT" | "SENT" | "ACCEPTED" | "REJECTED" | "EXPIRED" | "CONVERTED" | "ARCHIVED" | string;
  /** Quote title. */
  title?: string | null;
  /** Issue date (YYYY-MM-DD). */
  issueDate?: string | null;
  /** Valid until date (YYYY-MM-DD). */
  validUntil?: string | null;
  /** Currency code (e.g., EUR). */
  currency?: string | null;
  /** Total amount. */
  totalAmount?: number | null;
  /** Customer id. */
  customerId?: number | string | null;
  /** Customer display name. */
  customerDisplayName?: string | null;
}

/** Detailed response for a quote. */
export interface QuoteDetailResponse extends QuoteSummaryResponse {
  /** Notes. */
  notes?: string | null;
  /** Subtotal amount. */
  subtotalAmount?: number | null;
  /** Tax amount. */
  taxAmount?: number | null;
  /** Total amount. */
  totalAmount?: number | null;
  /** Created date-time. */
  createdAt?: string | null;
  /** Updated date-time. */
  updatedAt?: string | null;
}

/** Minimal invoice response returned by quote conversion. */
export interface InvoiceDetailResponse {
  /** Invoice identifier. */
  id: number | string;
  /** Issue date (optional). */
  issueDate?: string;
}

/** Quote-related API service. */
export class QuoteService extends BaseService {
  /** Base path for quote endpoints. */
  protected basePath(): string {
    return "/quotes";
  }

  /**
   * Creates a new quote.
   * @param request creation payload
   */
  createQuote(request: CreateQuoteRequest): Promise<QuoteDetailResponse> {
    return this.post<QuoteDetailResponse, CreateQuoteRequest>(this.endpoint(), { body: request });
  }

  /**
   * Lists quotes with pagination.
   * @param params pagination and query params
   */
  listQuotes(params: PageQuery = {}): Promise<Page<QuoteSummaryResponse>> {
    return this.get<Page<QuoteSummaryResponse>>(this.endpoint(), { query: params });
  }

  /**
   * Returns a quote by id.
   * @param quoteId quote identifier
   */
  getQuote(quoteId: number | string): Promise<QuoteDetailResponse> {
    return this.get<QuoteDetailResponse>(this.endpoint(`/${quoteId}`));
  }

  /**
   * Updates a quote by id.
   * @param quoteId quote identifier
   * @param request update payload
   */
  updateQuote(quoteId: number | string, request: UpdateQuoteRequest): Promise<QuoteDetailResponse> {
    return this.patch<QuoteDetailResponse, UpdateQuoteRequest>(this.endpoint(`/${quoteId}`), { body: request });
  }

  /**
   * Archives a quote.
   * @param quoteId quote identifier
   */
  archiveQuote(quoteId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${quoteId}/archive`));
  }

  /**
   * Marks a quote as sent.
   * @param quoteId quote identifier
   */
  sendQuote(quoteId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${quoteId}/send`));
  }

  /**
   * Marks a quote as accepted.
   * @param quoteId quote identifier
   */
  acceptQuote(quoteId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${quoteId}/accept`));
  }

  /**
   * Marks a quote as rejected.
   * @param quoteId quote identifier
   */
  rejectQuote(quoteId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${quoteId}/reject`));
  }

  /**
   * Converts a quote into an invoice.
   * @param quoteId quote identifier
   */
  convertToInvoice(quoteId: number | string): Promise<InvoiceDetailResponse> {
    return this.post<InvoiceDetailResponse, unknown>(this.endpoint(`/${quoteId}/convert`));
  }

  /**
   * Returns a quote PDF (inline).
   * @param quoteId quote identifier
   */
  getQuotePdf(quoteId: number | string): Promise<Blob> {
    return this.get<Blob>(this.endpoint(`/${quoteId}/pdf`), { responseType: "blob" });
  }

  /**
   * Returns a quote PDF as a download.
   * @param quoteId quote identifier
   */
  getQuotePdfDownload(quoteId: number | string): Promise<Blob> {
    return this.get<Blob>(this.endpoint(`/${quoteId}/pdf-download`), { responseType: "blob" });
  }
}
