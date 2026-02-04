package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;

// Una volta che diventa Overdue si puo comunque ancora pagare o archiviare
public class OverdueInvoiceState implements InvoiceState {
    @Override
    public void draft(Invoice invoice) {
        throw new IllegalStateException("Cannot draft an overdue invoice");
    }

    @Override
    public void issue(Invoice invoice) {
        throw new IllegalStateException("Cannot issue an overdue invoice");
    }

    @Override
    public void pay(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAID);
    }

    @Override
    public void overdue(Invoice invoice) {
        return;
    }

    @Override
    public void archive(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.ARCHIVED);
    }
}