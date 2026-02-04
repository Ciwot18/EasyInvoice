package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;

// Una volta che viene pagata il ciclo termina
public class PaidInvoiceState implements InvoiceState {
    @Override
    public void draft(Invoice invoice) {
        throw new IllegalStateException("Cannot draft a paid invoice");
    }

    @Override
    public void issue(Invoice invoice) {
        throw new IllegalStateException("Cannot issue a paid invoice");
    }

    @Override
    public void pay(Invoice invoice) {
        return;
    }

    @Override
    public void overdue(Invoice invoice) {
        throw new IllegalStateException("Cannot mark overdue a paid invoice");
    }

    @Override
    public void archive(Invoice invoice) {
        throw new IllegalStateException("Cannot archive a paid invoice");
    }
}