import { QuoteAction } from "./quote-action";
import { QuoteStatus } from "./quote-status";

/** Contract for quote state handlers. */
export interface QuoteState {
  /** Current status represented by this state. */
  status(): QuoteStatus;
  /** Returns available actions for this state (for UI). */
  availableActions(): QuoteAction[];
  /** Returns true if the action is allowed in this state. */
  can(action: QuoteAction): boolean;
  /** Returns the next status for an action, or throws if not allowed. */
  transition(action: QuoteAction): QuoteStatus;
}

/** Base class to share common logic between quote states. */
export abstract class BaseQuoteState implements QuoteState {
  abstract status(): QuoteStatus;
  protected abstract allowedActions(): QuoteAction[];
  protected abstract nextStatus(action: QuoteAction): QuoteStatus;

  availableActions(): QuoteAction[] {
    return this.allowedActions();
  }

  can(action: QuoteAction): boolean {
    return this.allowedActions().includes(action);
  }

  transition(action: QuoteAction): QuoteStatus {
    if (!this.can(action)) {
      throw new Error(`Action ${action} is not allowed for status ${this.status()}`);
    }
    return this.nextStatus(action);
  }
}
