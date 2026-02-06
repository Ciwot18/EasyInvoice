package com.kernith.easyinvoice.data.dto.dashboard;

import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;

/**
 * Aggregated quote totals for a specific status.
 *
 * @param status quote status
 * @param count number of quotes in the status
 * @param totalAmount sum of quote totals for the status
 */
public record QuoteStatusAggregateResponse(
        QuoteStatus status,
        long count,
        BigDecimal totalAmount
) {}
