package com.kernith.easyinvoice.data.model.state;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.state.invoice.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceStateTests {

    @Test
    void draftStateTransitions() {
        Invoice invoice = buildInvoice(InvoiceStatus.DRAFT);
        InvoiceState state = new DraftInvoiceState();

        state.draft(invoice);
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());

        state.issue(invoice);
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.DRAFT);
        state.archive(invoice);
        assertEquals(InvoiceStatus.ARCHIVED, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.DRAFT);
        assertThrows(IllegalStateException.class, () -> state.pay(invoice));
        assertThrows(IllegalStateException.class, () -> state.overdue(invoice));
    }

    @Test
    void issuedStateTransitions() {
        Invoice invoice = buildInvoice(InvoiceStatus.ISSUED);
        InvoiceState state = new IssuedInvoiceState();

        state.issue(invoice);
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());

        state.pay(invoice);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.ISSUED);
        state.overdue(invoice);
        assertEquals(InvoiceStatus.OVERDUE, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.ISSUED);
        assertThrows(IllegalStateException.class, () -> state.draft(invoice));
        assertThrows(IllegalStateException.class, () -> state.archive(invoice));
    }

    @Test
    void paidStateTransitions() {
        Invoice invoice = buildInvoice(InvoiceStatus.PAID);
        InvoiceState state = new PaidInvoiceState();

        state.pay(invoice);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());

        assertThrows(IllegalStateException.class, () -> state.draft(invoice));
        assertThrows(IllegalStateException.class, () -> state.issue(invoice));
        assertThrows(IllegalStateException.class, () -> state.overdue(invoice));
        assertThrows(IllegalStateException.class, () -> state.archive(invoice));
    }

    @Test
    void overdueStateTransitions() {
        Invoice invoice = buildInvoice(InvoiceStatus.OVERDUE);
        InvoiceState state = new OverdueInvoiceState();

        state.overdue(invoice);
        assertEquals(InvoiceStatus.OVERDUE, invoice.getStatus());

        state.pay(invoice);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.OVERDUE);
        state.archive(invoice);
        assertEquals(InvoiceStatus.ARCHIVED, invoice.getStatus());

        invoice.setStatus(InvoiceStatus.OVERDUE);
        assertThrows(IllegalStateException.class, () -> state.draft(invoice));
        assertThrows(IllegalStateException.class, () -> state.issue(invoice));
    }

    @Test
    void archivedStateTransitions() {
        Invoice invoice = buildInvoice(InvoiceStatus.ARCHIVED);
        InvoiceState state = new ArchivedInvoiceState();

        state.archive(invoice);
        assertEquals(InvoiceStatus.ARCHIVED, invoice.getStatus());

        assertThrows(IllegalStateException.class, () -> state.draft(invoice));
        assertThrows(IllegalStateException.class, () -> state.issue(invoice));
        assertThrows(IllegalStateException.class, () -> state.pay(invoice));
        assertThrows(IllegalStateException.class, () -> state.overdue(invoice));
    }

    private Invoice buildInvoice(InvoiceStatus status) {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);
        invoice.setStatus(status);
        return invoice;
    }
}
