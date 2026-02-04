package com.kernith.easyinvoice.data.model.state;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.model.state.quote.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuoteStateTests {

    @Test
    void draftStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.DRAFT);
        QuoteState state = new DraftQuoteState();

        state.draft(quote);
        assertEquals(QuoteStatus.DRAFT, quote.getStatus());

        state.send(quote);
        assertEquals(QuoteStatus.SENT, quote.getStatus());

        quote.setStatus(QuoteStatus.DRAFT);
        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.DRAFT);
        state.accept(quote);
        assertEquals(QuoteStatus.ACCEPTED, quote.getStatus());

        quote.setStatus(QuoteStatus.DRAFT);
        state.reject(quote);
        assertEquals(QuoteStatus.REJECTED, quote.getStatus());
        assertThrows(IllegalStateException.class, () -> state.expire(quote));
        assertThrows(IllegalStateException.class, () -> state.convert(quote));
    }

    @Test
    void sentStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.SENT);
        QuoteState state = new SentQuoteState();

        state.send(quote);
        assertEquals(QuoteStatus.SENT, quote.getStatus());

        state.accept(quote);
        assertEquals(QuoteStatus.ACCEPTED, quote.getStatus());

        quote.setStatus(QuoteStatus.SENT);
        state.reject(quote);
        assertEquals(QuoteStatus.REJECTED, quote.getStatus());

        quote.setStatus(QuoteStatus.SENT);
        state.expire(quote);
        assertEquals(QuoteStatus.EXPIRED, quote.getStatus());

        quote.setStatus(QuoteStatus.SENT);
        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.SENT);
        assertThrows(IllegalStateException.class, () -> state.draft(quote));
        assertThrows(IllegalStateException.class, () -> state.convert(quote));
    }

    @Test
    void acceptedStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.ACCEPTED);
        QuoteState state = new AcceptedQuoteState();

        state.accept(quote);
        assertEquals(QuoteStatus.ACCEPTED, quote.getStatus());

        state.convert(quote);
        assertEquals(QuoteStatus.CONVERTED, quote.getStatus());

        quote.setStatus(QuoteStatus.ACCEPTED);
        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.ACCEPTED);
        assertThrows(IllegalStateException.class, () -> state.draft(quote));
        assertThrows(IllegalStateException.class, () -> state.send(quote));
        assertThrows(IllegalStateException.class, () -> state.reject(quote));
        assertThrows(IllegalStateException.class, () -> state.expire(quote));
    }

    @Test
    void rejectedStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.REJECTED);
        QuoteState state = new RejectedQuoteState();

        state.reject(quote);
        assertEquals(QuoteStatus.REJECTED, quote.getStatus());

        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.REJECTED);
        state.draft(quote);
        assertEquals(QuoteStatus.DRAFT, quote.getStatus());

        quote.setStatus(QuoteStatus.REJECTED);
        assertThrows(IllegalStateException.class, () -> state.send(quote));
        assertThrows(IllegalStateException.class, () -> state.accept(quote));
        assertThrows(IllegalStateException.class, () -> state.expire(quote));
        assertThrows(IllegalStateException.class, () -> state.convert(quote));
    }

    @Test
    void expiredStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.EXPIRED);
        QuoteState state = new ExpiredQuoteState();

        state.expire(quote);
        assertEquals(QuoteStatus.EXPIRED, quote.getStatus());

        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.EXPIRED);
        assertThrows(IllegalStateException.class, () -> state.draft(quote));
        assertThrows(IllegalStateException.class, () -> state.send(quote));
        assertThrows(IllegalStateException.class, () -> state.accept(quote));
        assertThrows(IllegalStateException.class, () -> state.reject(quote));
        assertThrows(IllegalStateException.class, () -> state.convert(quote));
    }

    @Test
    void convertedStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.CONVERTED);
        QuoteState state = new ConvertedQuoteState();

        state.convert(quote);
        assertEquals(QuoteStatus.CONVERTED, quote.getStatus());

        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        quote.setStatus(QuoteStatus.CONVERTED);
        assertThrows(IllegalStateException.class, () -> state.draft(quote));
        assertThrows(IllegalStateException.class, () -> state.send(quote));
        assertThrows(IllegalStateException.class, () -> state.accept(quote));
        assertThrows(IllegalStateException.class, () -> state.reject(quote));
        assertThrows(IllegalStateException.class, () -> state.expire(quote));
    }

    @Test
    void archivedStateTransitions() {
        Quote quote = buildQuote(QuoteStatus.ARCHIVED);
        QuoteState state = new ArchivedQuoteState();

        state.archive(quote);
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());

        assertThrows(IllegalStateException.class, () -> state.draft(quote));
        assertThrows(IllegalStateException.class, () -> state.send(quote));
        assertThrows(IllegalStateException.class, () -> state.accept(quote));
        assertThrows(IllegalStateException.class, () -> state.reject(quote));
        assertThrows(IllegalStateException.class, () -> state.expire(quote));
        assertThrows(IllegalStateException.class, () -> state.convert(quote));
    }

    private Quote buildQuote(QuoteStatus status) {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        quote.setStatus(status);
        return quote;
    }
}
