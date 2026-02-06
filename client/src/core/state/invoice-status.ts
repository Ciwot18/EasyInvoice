/** Lifecycle status for invoices (mirrors backend InvoiceStatus). */
export enum InvoiceStatus {
  DRAFT = "DRAFT",
  ISSUED = "ISSUED",
  PAID = "PAID",
  OVERDUE = "OVERDUE",
  ARCHIVED = "ARCHIVED",
}
