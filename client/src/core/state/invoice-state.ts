import { InvoiceAction } from "./invoice-action";
import { InvoiceStatus } from "./invoice-status";

/** Contract for invoice state handlers. */
export interface InvoiceState {
  /** Current status represented by this state. */
  status(): InvoiceStatus;
  /** Returns available actions for this state (for UI). */
  availableActions(): InvoiceAction[];
  /** Returns true if the action is allowed in this state. */
  can(action: InvoiceAction): boolean;
  /** Returns the next status for an action, or throws if not allowed. */
  transition(action: InvoiceAction): InvoiceStatus;
}

/** Base class to share common logic between invoice states. */
export abstract class BaseInvoiceState implements InvoiceState {
  abstract status(): InvoiceStatus;
  protected abstract allowedActions(): InvoiceAction[];
  protected abstract nextStatus(action: InvoiceAction): InvoiceStatus;

  availableActions(): InvoiceAction[] {
    return this.allowedActions();
  }

  can(action: InvoiceAction): boolean {
    return this.allowedActions().includes(action);
  }

  transition(action: InvoiceAction): InvoiceStatus {
    if (!this.can(action)) {
      throw new Error(`Action ${action} is not allowed for status ${this.status()}`);
    }
    return this.nextStatus(action);
  }
}
