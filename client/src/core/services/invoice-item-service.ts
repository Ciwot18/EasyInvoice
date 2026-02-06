import { BaseService } from "./base-service";

/** Payload for creating an invoice item. */
export interface CreateInvoiceItemRequest {
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

/** Payload for updating an invoice item. */
export interface UpdateInvoiceItemRequest {
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

/** Response payload for an invoice item. */
export interface InvoiceItemResponse {
  /** Item identifier. */
  id: number;
  /** Invoice identifier. */
  invoiceId: number;
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

/** Invoice item API service. */
export class InvoiceItemService extends BaseService {
  /** Base path for invoice item endpoints. */
  protected basePath(): string {
    return "/invoices";
  }

  /**
   * Adds an item to an invoice.
   * @param invoiceId invoice identifier
   * @param request item payload
   */
  addItem(invoiceId: number | string, request: CreateInvoiceItemRequest): Promise<InvoiceItemResponse> {
    return this.post<InvoiceItemResponse, CreateInvoiceItemRequest>(this.endpoint(`/${invoiceId}/items`), {
      body: request,
    });
  }

  /**
   * Lists items for an invoice.
   * @param invoiceId invoice identifier
   */
  listItems(invoiceId: number | string): Promise<InvoiceItemResponse[]> {
    return this.get<InvoiceItemResponse[]>(this.endpoint(`/${invoiceId}/items`));
  }

  /**
   * Updates an item by id.
   * @param invoiceId invoice identifier
   * @param itemId item identifier
   * @param request update payload
   */
  updateItem(
    invoiceId: number | string,
    itemId: number | string,
    request: UpdateInvoiceItemRequest
  ): Promise<InvoiceItemResponse> {
    return this.patch<InvoiceItemResponse, UpdateInvoiceItemRequest>(
      this.endpoint(`/${invoiceId}/items/${itemId}`),
      { body: request }
    );
  }

  /**
   * Deletes an item from an invoice.
   * @param invoiceId invoice identifier
   * @param itemId item identifier
   */
  deleteItem(invoiceId: number | string, itemId: number | string): Promise<void> {
    return this.delete<void>(this.endpoint(`/${invoiceId}/items/${itemId}`));
  }
}
