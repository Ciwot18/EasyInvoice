package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

/**
 * Draft state: the quote can be sent, accepted, rejected, or archived.
 */
public class DraftQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        return;
    }

    @Override
    public void send(Quote quote) {
        quote.setStatus(QuoteStatus.SENT);
    }

    @Override
    public void accept(Quote quote) {
        quote.setStatus(QuoteStatus.ACCEPTED);
    }

    @Override
    public void reject(Quote quote) {
        quote.setStatus(QuoteStatus.REJECTED);
    }

    @Override
    public void expire(Quote quote) {
        throw new IllegalStateException("Cannot expire a draft quote before sending");
    }

    @Override
    public void convert(Quote quote) {
        throw new IllegalStateException("Cannot convert a draft quote before acceptance");
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}
