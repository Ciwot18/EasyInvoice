package com.kernith.easyinvoice.data.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Summary payload for invoices of a specific customer.
 *
 * @param customerId customer identifier
 * @param invoices invoice aggregates by status
 * @param paidTotal total of paid invoices
 * @param issuedTotal total of issued invoices (excluding overdue)
 * @param outstandingTotal total of issued + overdue invoices
 */
public record CustomerInvoiceSummaryResponse(
        Long customerId,
        List<InvoiceStatusAggregateResponse> invoices,
        BigDecimal paidTotal,
        BigDecimal issuedTotal,
        BigDecimal outstandingTotal
) {}
