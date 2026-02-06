import { InvoiceAction } from "./invoice-action";
import { InvoiceStatus } from "./invoice-status";
import { BaseInvoiceState } from "./invoice-state";

/** Draft state: can issue or archive. */
export class DraftInvoiceState extends BaseInvoiceState {
  status(): InvoiceStatus {
    return InvoiceStatus.DRAFT;
  }

  protected allowedActions(): InvoiceAction[] {
    return [InvoiceAction.ISSUE, InvoiceAction.ARCHIVE];
  }

  protected nextStatus(action: InvoiceAction): InvoiceStatus {
    switch (action) {
      case InvoiceAction.ISSUE:
        return InvoiceStatus.ISSUED;
      case InvoiceAction.ARCHIVE:
        return InvoiceStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Issued state: can be paid or marked overdue. */
export class IssuedInvoiceState extends BaseInvoiceState {
  status(): InvoiceStatus {
    return InvoiceStatus.ISSUED;
  }

  protected allowedActions(): InvoiceAction[] {
    return [InvoiceAction.PAY, InvoiceAction.OVERDUE];
  }

  protected nextStatus(action: InvoiceAction): InvoiceStatus {
    switch (action) {
      case InvoiceAction.PAY:
        return InvoiceStatus.PAID;
      case InvoiceAction.OVERDUE:
        return InvoiceStatus.OVERDUE;
      default:
        return this.status();
    }
  }
}

/** Overdue state: can be paid or archived. */
export class OverdueInvoiceState extends BaseInvoiceState {
  status(): InvoiceStatus {
    return InvoiceStatus.OVERDUE;
  }

  protected allowedActions(): InvoiceAction[] {
    return [InvoiceAction.PAY, InvoiceAction.ARCHIVE];
  }

  protected nextStatus(action: InvoiceAction): InvoiceStatus {
    switch (action) {
      case InvoiceAction.PAY:
        return InvoiceStatus.PAID;
      case InvoiceAction.ARCHIVE:
        return InvoiceStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Paid state: no further lifecycle transitions. */
export class PaidInvoiceState extends BaseInvoiceState {
  status(): InvoiceStatus {
    return InvoiceStatus.PAID;
  }

  protected allowedActions(): InvoiceAction[] {
    return [];
  }

  protected nextStatus(_action: InvoiceAction): InvoiceStatus {
    return this.status();
  }
}

/** Archived state: no further lifecycle transitions. */
export class ArchivedInvoiceState extends BaseInvoiceState {
  status(): InvoiceStatus {
    return InvoiceStatus.ARCHIVED;
  }

  protected allowedActions(): InvoiceAction[] {
    return [];
  }

  protected nextStatus(_action: InvoiceAction): InvoiceStatus {
    return this.status();
  }
}
