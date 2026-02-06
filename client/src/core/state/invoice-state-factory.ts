import { InvoiceStatus } from "./invoice-status";
import { InvoiceState } from "./invoice-state";
import {
  ArchivedInvoiceState,
  DraftInvoiceState,
  IssuedInvoiceState,
  OverdueInvoiceState,
  PaidInvoiceState,
} from "./invoice-states";

/** Factory that maps invoice status to a state handler. */
export class InvoiceStateFactory {
  private constructor() {}

  /**
   * @param status current invoice status
   */
  static from(status: InvoiceStatus): InvoiceState {
    switch (status) {
      case InvoiceStatus.DRAFT:
        return new DraftInvoiceState();
      case InvoiceStatus.ISSUED:
        return new IssuedInvoiceState();
      case InvoiceStatus.PAID:
        return new PaidInvoiceState();
      case InvoiceStatus.OVERDUE:
        return new OverdueInvoiceState();
      case InvoiceStatus.ARCHIVED:
        return new ArchivedInvoiceState();
      default:
        return new DraftInvoiceState();
    }
  }
}
