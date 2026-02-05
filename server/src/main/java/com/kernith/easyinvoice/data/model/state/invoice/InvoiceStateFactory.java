package com.kernith.easyinvoice.data.model.state.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;

/**
 * Factory that maps invoice status to the appropriate state handler.
 */
public class InvoiceStateFactory {
    private InvoiceStateFactory() {}

    public static InvoiceState from(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice is required");
        }
        return from(invoice.getStatus());
    }

    public static InvoiceState from(InvoiceStatus status) {
        return switch (status) {
            case DRAFT -> new DraftInvoiceState();
            case ISSUED -> new IssuedInvoiceState();
            case PAID -> new PaidInvoiceState();
            case OVERDUE -> new OverdueInvoiceState();
            case ARCHIVED -> new ArchivedInvoiceState();
        };
    }
}
