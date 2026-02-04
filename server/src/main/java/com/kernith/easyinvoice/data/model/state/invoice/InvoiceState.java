package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;

public interface InvoiceState {
    void draft(Invoice invoice);
    void issue(Invoice invoice);
    void pay(Invoice invoice);
    void overdue(Invoice invoice);
    void archive(Invoice invoice);
}
