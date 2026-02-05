package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

/**
 * Factory that maps quote status to the appropriate state handler.
 */
public class QuoteStateFactory {
    private QuoteStateFactory() {}

    public static QuoteState from(Quote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("Quote is required");
        }
        return from(quote.getStatus());
    }

    public static QuoteState from(QuoteStatus status) {
        return switch (status) {
            case DRAFT      -> new DraftQuoteState();
            case SENT       -> new SentQuoteState();
            case ACCEPTED   -> new AcceptedQuoteState();
            case REJECTED   -> new RejectedQuoteState();
            case EXPIRED    -> new ExpiredQuoteState();
            case CONVERTED  -> new ConvertedQuoteState();
            case ARCHIVED   -> new ArchivedQuoteState();
        };
    }
}
