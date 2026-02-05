package com.kernith.easyinvoice.data.model.state;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.model.state.invoice.ArchivedInvoiceState;
import com.kernith.easyinvoice.data.model.state.invoice.DraftInvoiceState;
import com.kernith.easyinvoice.data.model.state.invoice.InvoiceStateFactory;
import com.kernith.easyinvoice.data.model.state.invoice.IssuedInvoiceState;
import com.kernith.easyinvoice.data.model.state.invoice.OverdueInvoiceState;
import com.kernith.easyinvoice.data.model.state.invoice.PaidInvoiceState;
import com.kernith.easyinvoice.data.model.state.quote.AcceptedQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.ArchivedQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.ConvertedQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.DraftQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.ExpiredQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.QuoteStateFactory;
import com.kernith.easyinvoice.data.model.state.quote.RejectedQuoteState;
import com.kernith.easyinvoice.data.model.state.quote.SentQuoteState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StateFactoryTests {

    @Test
    void invoiceStateFactoryCreatesExpectedStates() {
        assertInstanceOf(DraftInvoiceState.class, InvoiceStateFactory.from(InvoiceStatus.DRAFT));
        assertInstanceOf(IssuedInvoiceState.class, InvoiceStateFactory.from(InvoiceStatus.ISSUED));
        assertInstanceOf(PaidInvoiceState.class, InvoiceStateFactory.from(InvoiceStatus.PAID));
        assertInstanceOf(OverdueInvoiceState.class, InvoiceStateFactory.from(InvoiceStatus.OVERDUE));
        assertInstanceOf(ArchivedInvoiceState.class, InvoiceStateFactory.from(InvoiceStatus.ARCHIVED));
    }

    @Test
    void invoiceStateFactoryThrowsOnNullInvoice() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceStateFactory.from((Invoice) null));
    }

    @Test
    void invoiceStateFactoryFromInvoiceUsesStatus() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);
        invoice.setStatus(InvoiceStatus.ISSUED);

        assertInstanceOf(IssuedInvoiceState.class, InvoiceStateFactory.from(invoice));
    }

    @Test
    void quoteStateFactoryCreatesExpectedStates() {
        assertInstanceOf(DraftQuoteState.class, QuoteStateFactory.from(QuoteStatus.DRAFT));
        assertInstanceOf(SentQuoteState.class, QuoteStateFactory.from(QuoteStatus.SENT));
        assertInstanceOf(AcceptedQuoteState.class, QuoteStateFactory.from(QuoteStatus.ACCEPTED));
        assertInstanceOf(RejectedQuoteState.class, QuoteStateFactory.from(QuoteStatus.REJECTED));
        assertInstanceOf(ExpiredQuoteState.class, QuoteStateFactory.from(QuoteStatus.EXPIRED));
        assertInstanceOf(ConvertedQuoteState.class, QuoteStateFactory.from(QuoteStatus.CONVERTED));
        assertInstanceOf(ArchivedQuoteState.class, QuoteStateFactory.from(QuoteStatus.ARCHIVED));
    }

    @Test
    void quoteStateFactoryThrowsOnNullQuote() {
        assertThrows(IllegalArgumentException.class, () -> QuoteStateFactory.from((Quote) null));
    }

    @Test
    void quoteStateFactoryFromQuoteUsesStatus() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        quote.setStatus(QuoteStatus.SENT);

        assertInstanceOf(SentQuoteState.class, QuoteStateFactory.from(quote));
    }
}
