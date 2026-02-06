import { ApiClient } from "@/src/core/api";
import { BaseService } from "./base-service";

/** Payload for creating a quote item. */
export interface CreateQuoteItemRequest {
  /** Position index (1-based). */
  position: number;
  /** Item description. */
  description: string;
  /** Notes (optional). */
  notes?: string;
  /** Quantity. */
  quantity?: number;
  /** Unit (optional). */
  unit?: string;
  /** Unit price. */
  unitPrice?: number;
  /** Tax rate (optional). */
  taxRate?: number;
  /** Discount type (optional). */
  discountType?: "NONE" | "PERCENT" | "AMOUNT";
  /** Discount value (optional). */
  discountValue?: number;
}

/** Payload for updating a quote item. */
export interface UpdateQuoteItemRequest {
  /** Position index (1-based). */
  position?: number;
  /** Updated description. */
  description?: string;
  /** Updated notes. */
  notes?: string;
  /** Updated quantity. */
  quantity?: number;
  /** Updated unit. */
  unit?: string;
  /** Updated unit price. */
  unitPrice?: number;
  /** Updated tax rate. */
  taxRate?: number;
  /** Updated discount type. */
  discountType?: "NONE" | "PERCENT" | "AMOUNT";
  /** Updated discount value. */
  discountValue?: number;
}

/** Response payload for a quote item. */
export interface QuoteItemResponse {
  /** Item identifier. */
  id: number;
  /** Quote identifier. */
  quoteId: number;
  /** Position index (1-based). */
  position: number;
  /** Item description (optional). */
  description?: string;
  /** Notes (optional). */
  notes?: string;
  /** Quantity (optional). */
  quantity?: number;
  /** Unit (optional). */
  unit?: string;
  /** Unit price (optional). */
  unitPrice?: number;
  /** Tax rate (optional). */
  taxRate?: number;
  /** Discount type (optional). */
  discountType?: "NONE" | "PERCENT" | "AMOUNT";
  /** Discount value (optional). */
  discountValue?: number;
  /** Line subtotal amount. */
  lineSubtotalAmount?: number;
  /** Line tax amount. */
  lineTaxAmount?: number;
  /** Line total amount. */
  lineTotalAmount?: number;
}

/** Quote item API service. */
export class QuoteItemService extends BaseService {
  private readonly base: string;

  /**
   * @param client API client
   * @param basePathOverride optional base path (default: "/quotes")
   */
  constructor(client: ApiClient, basePathOverride: string = "/quotes") {
    super(client);
    this.base = basePathOverride;
  }

  /** Base path for quote item endpoints. */
  protected basePath(): string {
    return this.base;
  }

  /**
   * Adds an item to a quote.
   * @param quoteId quote identifier
   * @param request item payload
   */
  addItem(quoteId: number | string, request: CreateQuoteItemRequest): Promise<QuoteItemResponse> {
    return this.post<QuoteItemResponse, CreateQuoteItemRequest>(this.endpoint(`/${quoteId}/items`), {
      body: request,
    });
  }

  /**
   * Lists items for a quote.
   * @param quoteId quote identifier
   */
  listItems(quoteId: number | string): Promise<QuoteItemResponse[]> {
    return this.get<QuoteItemResponse[]>(this.endpoint(`/${quoteId}/items`));
  }

  /**
   * Updates a quote item by id.
   * @param quoteId quote identifier
   * @param itemId item identifier
   * @param request update payload
   */
  updateItem(quoteId: number | string, itemId: number | string, request: UpdateQuoteItemRequest): Promise<QuoteItemResponse> {
    return this.patch<QuoteItemResponse, UpdateQuoteItemRequest>(
      this.endpoint(`/${quoteId}/items/${itemId}`),
      { body: request }
    );
  }

  /**
   * Deletes an item from a quote.
   * @param quoteId quote identifier
   * @param itemId item identifier
   */
  deleteItem(quoteId: number | string, itemId: number | string): Promise<void> {
    return this.delete<void>(this.endpoint(`/${quoteId}/items/${itemId}`));
  }
}
