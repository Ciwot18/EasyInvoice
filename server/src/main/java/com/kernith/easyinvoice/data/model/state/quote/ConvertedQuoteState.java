package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

/**
 * Converted state: the quote has been turned into an invoice and can only be archived.
 */
public class ConvertedQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        throw new IllegalStateException("Cannot draft a converted quote");
    }

    @Override
    public void send(Quote quote) {
        throw new IllegalStateException("Cannot send a converted quote");
    }

    @Override
    public void accept(Quote quote) {
        throw new IllegalStateException("Cannot accept a converted quote");
    }

    @Override
    public void reject(Quote quote) {
        throw new IllegalStateException("Cannot reject a converted quote");
    }

    @Override
    public void expire(Quote quote) {
        throw new IllegalStateException("Cannot expire a converted quote");
    }

    @Override
    public void convert(Quote quote) {
        return;
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}
