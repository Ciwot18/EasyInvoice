package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;

// Arrivato in archivio la fattura finisce il suo ciclo
public class ArchivedInvoiceState implements InvoiceState {
    @Override
    public void draft(Invoice invoice) {
        throw new IllegalStateException("Cannot draft an archived Invoice");
    }

    @Override
    public void issue(Invoice invoice) {
        throw new IllegalStateException("Cannot issue an archived Invoice");
    }

    @Override
    public void pay(Invoice invoice) {
        throw new IllegalStateException("Cannot pay an archived Invoice");
    }

    @Override
    public void overdue(Invoice invoice) {
        throw new IllegalStateException("Cannot mark overdue an archived invoice");
    }

    @Override
    public void archive(Invoice invoice) {
        return;
    }
}