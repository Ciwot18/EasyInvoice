import { QuoteStatus } from "./quote-status";
import { QuoteState } from "./quote-state";
import {
  AcceptedQuoteState,
  ArchivedQuoteState,
  ConvertedQuoteState,
  DraftQuoteState,
  ExpiredQuoteState,
  RejectedQuoteState,
  SentQuoteState,
} from "./quote-states";

/** Factory that maps quote status to a state handler. */
export class QuoteStateFactory {
  private constructor() {}

  /**
   * @param status current quote status
   */
  static from(status: QuoteStatus): QuoteState {
    switch (status) {
      case QuoteStatus.DRAFT:
        return new DraftQuoteState();
      case QuoteStatus.SENT:
        return new SentQuoteState();
      case QuoteStatus.ACCEPTED:
        return new AcceptedQuoteState();
      case QuoteStatus.REJECTED:
        return new RejectedQuoteState();
      case QuoteStatus.EXPIRED:
        return new ExpiredQuoteState();
      case QuoteStatus.CONVERTED:
        return new ConvertedQuoteState();
      case QuoteStatus.ARCHIVED:
        return new ArchivedQuoteState();
      default:
        return new DraftQuoteState();
    }
  }
}
