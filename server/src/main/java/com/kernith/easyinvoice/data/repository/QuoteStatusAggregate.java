package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;

/**
 * Projection for quote aggregate queries.
 */
public interface QuoteStatusAggregate {
    QuoteStatus getStatus();
    Long getCount();
    BigDecimal getTotalAmount();
}
