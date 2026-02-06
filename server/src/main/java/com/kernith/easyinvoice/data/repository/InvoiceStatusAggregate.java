package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;

/**
 * Projection for invoice aggregate queries.
 */
public interface InvoiceStatusAggregate {
    InvoiceStatus getStatus();
    Long getCount();
    BigDecimal getTotalAmount();
}
