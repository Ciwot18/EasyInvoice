package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;

// Se il preventivo viene accettato allora si puo convertire o archiviare
public class AcceptedQuoteState implements QuoteState {
    @Override
    public void draft(Quote quote) {
        throw new IllegalStateException("Cannot draft an accepted quote");
    }

    @Override
    public void send(Quote quote) {
        throw new IllegalStateException("Cannot send an accepted quote");
    }

    @Override
    public void accept(Quote quote) {
        return;
    }

    @Override
    public void reject(Quote quote) {
        throw new IllegalStateException("Cannot reject an accepted quote");
    }

    @Override
    public void expire(Quote quote) {
        throw new IllegalStateException("Cannot expire an accepted quote");
    }

    @Override
    public void convert(Quote quote) {
        quote.setStatus(QuoteStatus.CONVERTED);
    }

    @Override
    public void archive(Quote quote) {
        quote.setStatus(QuoteStatus.ARCHIVED);
    }
}
