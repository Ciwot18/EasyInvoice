/** Lifecycle status for quotes (mirrors backend QuoteStatus). */
export enum QuoteStatus {
  DRAFT = "DRAFT",
  SENT = "SENT",
  ACCEPTED = "ACCEPTED",
  REJECTED = "REJECTED",
  EXPIRED = "EXPIRED",
  CONVERTED = "CONVERTED",
  ARCHIVED = "ARCHIVED",
}
