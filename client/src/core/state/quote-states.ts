import { QuoteAction } from "./quote-action";
import { QuoteStatus } from "./quote-status";
import { BaseQuoteState } from "./quote-state";

/** Draft state: can be sent, accepted, rejected, or archived. */
export class DraftQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.DRAFT;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.SEND, QuoteAction.ACCEPT, QuoteAction.REJECT, QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.SEND:
        return QuoteStatus.SENT;
      case QuoteAction.ACCEPT:
        return QuoteStatus.ACCEPTED;
      case QuoteAction.REJECT:
        return QuoteStatus.REJECTED;
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Sent state: can be accepted, rejected, expired, or archived. */
export class SentQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.SENT;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.ACCEPT, QuoteAction.REJECT, QuoteAction.EXPIRE, QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.ACCEPT:
        return QuoteStatus.ACCEPTED;
      case QuoteAction.REJECT:
        return QuoteStatus.REJECTED;
      case QuoteAction.EXPIRE:
        return QuoteStatus.EXPIRED;
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Accepted state: can be converted or archived. */
export class AcceptedQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.ACCEPTED;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.CONVERT, QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.CONVERT:
        return QuoteStatus.CONVERTED;
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Rejected state: can return to draft or be archived. */
export class RejectedQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.REJECTED;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.DRAFT, QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.DRAFT:
        return QuoteStatus.DRAFT;
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Expired state: can only be archived. */
export class ExpiredQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.EXPIRED;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Converted state: can only be archived. */
export class ConvertedQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.CONVERTED;
  }

  protected allowedActions(): QuoteAction[] {
    return [QuoteAction.ARCHIVE];
  }

  protected nextStatus(action: QuoteAction): QuoteStatus {
    switch (action) {
      case QuoteAction.ARCHIVE:
        return QuoteStatus.ARCHIVED;
      default:
        return this.status();
    }
  }
}

/** Archived state: no further lifecycle transitions. */
export class ArchivedQuoteState extends BaseQuoteState {
  status(): QuoteStatus {
    return QuoteStatus.ARCHIVED;
  }

  protected allowedActions(): QuoteAction[] {
    return [];
  }

  protected nextStatus(_action: QuoteAction): QuoteStatus {
    return this.status();
  }
}
