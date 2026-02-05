package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

/**
 * Expired state: the quote can only be archived.
 */
public class ExpiredQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        throw new IllegalStateException("Cannot draft an expired quote");
    }

    @Override
    public void send(Quote quote) {
        throw new IllegalStateException("Cannot send an expired quote");
    }

    @Override
    public void accept(Quote quote) {
        throw new IllegalStateException("Cannot accept an expired quote");
    }

    @Override
    public void reject(Quote quote) {
        throw new IllegalStateException("Cannot reject an expired quote");
    }

    @Override
    public void expire(Quote quote) {
        return;
    }

    @Override
    public void convert(Quote quote) {
        throw new IllegalStateException("Cannot convert an expired quote");
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}
