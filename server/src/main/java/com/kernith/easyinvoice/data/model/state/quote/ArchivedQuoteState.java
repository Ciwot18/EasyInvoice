package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;

/**
 * Archived state: the quote is read-only and cannot be reopened.
 */
public class ArchivedQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        throw new IllegalStateException("Cannot draft an archived quote");
    }

    @Override
    public void send(Quote quote) {
        throw new IllegalStateException("Cannot send an archived quote");
    }

    @Override
    public void accept(Quote quote) {
        throw new IllegalStateException("Cannot accept an archived quote");
    }

    @Override
    public void reject(Quote quote) {
        throw new IllegalStateException("Cannot reject an archived quote");
    }

    @Override
    public void expire(Quote quote) {
        throw new IllegalStateException("Cannot expire an archived quote");
    }

    @Override
    public void convert(Quote quote) {
        throw new IllegalStateException("Cannot convert an archived quote");
    }

    @Override
    public void archive(Quote quote) {
        return;
    }
}
