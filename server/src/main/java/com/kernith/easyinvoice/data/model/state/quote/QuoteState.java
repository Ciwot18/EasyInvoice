package com.kernith.easyinvoice.data.model.state.quote;

import com.kernith.easyinvoice.data.model.Quote;

/**
 * State contract for quote lifecycle transitions.
 */
public interface QuoteState {
    void draft(Quote quote);
    void send(Quote quote);
    void accept(Quote quote);
    void reject(Quote quote);
    void expire(Quote quote);
    void convert(Quote quote);
    void archive(Quote quote);
}
