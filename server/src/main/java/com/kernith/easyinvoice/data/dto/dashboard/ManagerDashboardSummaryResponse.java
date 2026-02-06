package com.kernith.easyinvoice.data.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Summary payload for the company manager dashboard.
 *
 * @param quotes quote aggregates by status
 * @param invoices invoice aggregates by status
 * @param paidTotal total of paid invoices
 * @param issuedTotal total of issued invoices (excluding overdue)
 * @param outstandingTotal total of issued + overdue invoices
 */
public record ManagerDashboardSummaryResponse(
        List<QuoteStatusAggregateResponse> quotes,
        List<InvoiceStatusAggregateResponse> invoices,
        BigDecimal paidTotal,
        BigDecimal issuedTotal,
        BigDecimal outstandingTotal
) {}
