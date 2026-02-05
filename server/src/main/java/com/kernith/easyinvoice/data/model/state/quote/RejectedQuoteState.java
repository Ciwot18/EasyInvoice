package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

/**
 * Rejected state: the quote can return to draft or be archived.
 */
public class RejectedQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        quote.setStatus(QuoteStatus.DRAFT);
    }

    @Override
    public void send(Quote quote) {
        throw new IllegalStateException("Cannot send a rejected quote");
    }

    @Override
    public void accept(Quote quote) {
        throw new IllegalStateException("Cannot accept a rejected quote");
    }

    @Override
    public void reject(Quote quote) {
        return;
    }

    @Override
    public void expire(Quote quote) {
        throw new IllegalStateException("Cannot expire a rejected quote");
    }

    @Override
    public void convert(Quote quote) {
        throw new IllegalStateException("Cannot convert a rejected quote");
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}
