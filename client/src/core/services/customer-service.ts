import { BaseService } from "./base-service";
import { Page, PageQuery } from "@/src/core/models/common";

/** Payload for creating a customer. */
export interface CreateCustomerRequest {
  /** Customer display name. */
  displayName: string;
  /** Customer legal name (optional). */
  legalName?: string;
  /** VAT number. */
  vatNumber: string;
  /** Customer email (optional). */
  email?: string;
  /** Phone (optional). */
  phone?: string;
  /** PEC email (optional). */
  pec?: string;
  /** Address (optional). */
  address?: string;
  /** City (optional). */
  city?: string;
  /** Postal code (optional). */
  postalCode?: string;
  /** Country code (optional). */
  country?: string;
  /** Status (optional). */
  status?: "ACTIVE" | "ARCHIVED" | "DELETED";
}

/** Payload for updating a customer. */
export interface UpdateCustomerRequest {
  /** Updated display name. */
  displayName?: string;
  /** Updated legal name. */
  legalName?: string;
  /** Updated VAT number. */
  vatNumber?: string;
  /** Updated email. */
  email?: string;
  /** Updated phone. */
  phone?: string;
  /** Updated PEC. */
  pec?: string;
  /** Updated address. */
  address?: string;
  /** Updated city. */
  city?: string;
  /** Updated postal code. */
  postalCode?: string;
  /** Updated country code. */
  country?: string;
}

/** Summary response for a customer. */
export interface CustomerSummaryResponse {
  /** Customer identifier. */
  id: number;
  /** Customer display name. */
  displayName: string;
  /** Customer legal name (optional). */
  legalName?: string | null;
  /** VAT number (optional). */
  vatNumber?: string | null;
  /** PEC email (optional). */
  pec?: string | null;
  /** Country code (optional). */
  country?: string | null;
}

/** Detailed response for a customer. */
export interface CustomerDetailResponse extends CustomerSummaryResponse {
  /** Status enum string. */
  status?: "ACTIVE" | "ARCHIVED" | "DELETED" | string;
  /** Customer email (optional). */
  email?: string | null;
  /** Phone (optional). */
  phone?: string | null;
  /** Address (optional). */
  address?: string | null;
  /** City (optional). */
  city?: string | null;
  /** Postal code (optional). */
  postalCode?: string | null;
  /** Created date-time. */
  createdAt?: string | null;
  /** Updated date-time. */
  updatedAt?: string | null;
}

/** Summary response for a quote. */
export interface QuoteSummaryResponse {
  /** Quote identifier. */
  id: number;
  /** Quote year. */
  quoteYear?: number | null;
  /** Quote number. */
  quoteNumber?: number | null;
  /** Status enum string. */
  status?: string | null;
  /** Title (optional). */
  title?: string | null;
  /** Issue date (optional). */
  issueDate?: string | null;
  /** Valid until (optional). */
  validUntil?: string | null;
  /** Currency (optional). */
  currency?: string | null;
  /** Total amount (optional). */
  totalAmount?: number | null;
  /** Customer id (optional). */
  customerId?: number | null;
  /** Customer display name (optional). */
  customerDisplayName?: string | null;
}

/** Customer-related API service. */
export class CustomerService extends BaseService {
  /** Base path for customer endpoints. */
  protected basePath(): string {
    return "/manager/customers";
  }

  /**
   * Creates a new customer.
   * @param request creation payload
   */
  createCustomer(request: CreateCustomerRequest): Promise<CustomerSummaryResponse> {
    return this.post<CustomerSummaryResponse, CreateCustomerRequest>(this.endpoint(), { body: request });
  }

  /**
   * Returns a customer by id.
   * @param customerId customer identifier
   */
  getCustomer(customerId: number | string): Promise<CustomerDetailResponse> {
    return this.get<CustomerDetailResponse>(this.endpoint(`/${customerId}`));
  }

  /**
   * Updates a customer by id.
   * @param customerId customer identifier
   * @param request update payload
   */
  updateCustomer(customerId: number | string, request: UpdateCustomerRequest): Promise<CustomerDetailResponse> {
    return this.patch<CustomerDetailResponse, UpdateCustomerRequest>(this.endpoint(`/${customerId}`), {
      body: request,
    });
  }

  /**
   * Deletes (soft-delete) a customer.
   * @param customerId customer identifier
   */
  deleteCustomer(customerId: number | string): Promise<void> {
    return this.delete<void>(this.endpoint(`/${customerId}`));
  }

  /**
   * Lists customers with detail payloads (default).
   * @param params pagination and query params
   */
  listCustomers(params?: PageQuery): Promise<Page<CustomerDetailResponse>>;
  /**
   * Lists customers with summary payloads.
   * @param params pagination and query params with type="summary"
   */
  listCustomers(params: PageQuery & { type: "summary" }): Promise<Page<CustomerSummaryResponse>>;
  /**
   * Lists customers with either summary or detail payloads based on `type`.
   * @param params pagination and query params
   */
  listCustomers(params: PageQuery & { type?: "summary" } = {}): Promise<Page<CustomerDetailResponse> | Page<CustomerSummaryResponse>> {
    const query = { ...params } as Record<string, string | number | boolean | null | undefined>;
    if (params.type === "summary") {
      return this.get<Page<CustomerSummaryResponse>>(this.endpoint(), { query: { ...query, type: "summary" } });
    }
    return this.get<Page<CustomerDetailResponse>>(this.endpoint(), { query });
  }

  /**
   * Archives a customer.
   * @param customerId customer identifier
   */
  archiveCustomer(customerId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${customerId}/archive`));
  }

  /**
   * Restores an archived customer.
   * @param customerId customer identifier
   */
  restoreCustomer(customerId: number | string): Promise<void> {
    return this.post<void, unknown>(this.endpoint(`/${customerId}/restore`));
  }

  /**
   * Lists quotes for a given customer.
   * @param customerId customer identifier
   */
  listCustomerQuotes(customerId: number | string): Promise<QuoteSummaryResponse[]> {
    return this.get<QuoteSummaryResponse[]>(`/customer/${customerId}/quotes`);
  }
}
