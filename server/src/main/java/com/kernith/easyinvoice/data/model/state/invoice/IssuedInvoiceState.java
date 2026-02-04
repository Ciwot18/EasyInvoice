package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;

// Una volta Issued puo solamente essere pagata o scadere ma non si puo piu rettificare
public class IssuedInvoiceState implements InvoiceState {
    @Override
    public void draft(Invoice invoice) {
        throw new IllegalStateException("Cannot draft an issued invoice");
    }

    @Override
    public void issue(Invoice invoice) {
        return;
    }

    @Override
    public void pay(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAID);
    }

    @Override
    public void overdue(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.OVERDUE);
    }

    @Override
    public void archive(Invoice invoice) {
        throw new IllegalStateException("Cannot archive an issued invoice");
    }
}