package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;

// In draft posso solamente inviare la fattura al cliente o archiviarla
public class DraftInvoiceState implements InvoiceState {
    @Override
    public void draft(Invoice invoice) {
        return;
    }

    @Override
    public void issue(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.ISSUED);
    }

    @Override
    public void pay(Invoice invoice) {
        throw new IllegalStateException("Cannot pay a draft invoice before issuing");
    }

    @Override
    public void overdue(Invoice invoice) {
        throw new IllegalStateException("Cannot mark overdue a draft invoice before issuing");
    }

    @Override
    public void archive(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.ARCHIVED);
    }
}