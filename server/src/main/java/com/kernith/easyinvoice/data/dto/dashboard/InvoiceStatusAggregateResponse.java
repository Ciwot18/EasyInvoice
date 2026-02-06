package com.kernith.easyinvoice.data.dto.dashboard;

import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;

/**
 * Aggregated invoice totals for a specific status.
 *
 * @param status invoice status
 * @param count number of invoices in the status
 * @param totalAmount sum of invoice totals for the status
 */
public record InvoiceStatusAggregateResponse(
        InvoiceStatus status,
        long count,
        BigDecimal totalAmount
) {}
