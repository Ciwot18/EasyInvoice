package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

// Un preventivo inviato puo essere accettato/rifiutato altrimenti puo scadere e/o venire archiviato
public class SentQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        throw new IllegalStateException("Cannot draft a sent quote before acceptance or rejection");
    }

    @Override
    public void send(Quote quote) {
        return;
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
        quote.setStatus(QuoteStatus.EXPIRED);
    }

    @Override
    public void convert(Quote quote) {
        throw new IllegalStateException("Cannot convert a sent quote before acceptance or rejection");
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}