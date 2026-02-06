/**
 * API Shim Layer
 *
 * This module provides a single import point for the API layer.
 * In this project we rely on the real services under `src/core`.
 */

export { services } from "@/src/core/api/runtime";
export { onApiError } from "@/src/core/api/api-events";
export type { ApiErrorEventDetail } from "@/src/core/api/api-events";

export type {
  QuoteSummaryResponse,
  QuoteDetailResponse,
} from "@/src/core/services/quote-service";
export type { QuoteItemResponse } from "@/src/core/services/quote-item-service";
export type {
  InvoiceSummaryResponse,
  InvoiceDetailResponse,
  InvoicePdfArchiveSaveResponse,
  InvoicePdfDto,
} from "@/src/core/services/invoice-service";
export type { InvoiceItemResponse } from "@/src/core/services/invoice-item-service";
export type {
  CustomerDetailResponse,
  CustomerSummaryResponse,
} from "@/src/core/services/customer-service";
export type { UserSummary, ProfileResponse } from "@/src/core/services/user-service";
export type { CompanySummaryResponse, CompanyDetailResponse } from "@/src/core/services/company-service";
