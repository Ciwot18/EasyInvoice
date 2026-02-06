package com.kernith.easyinvoice.data.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record CustomerInvoiceSummaryResponse(
        Long customerId,
        List<InvoiceStatusAggregateResponse> invoices,
        BigDecimal paidTotal,
        BigDecimal issuedTotal,
        BigDecimal outstandingTotal
) {}
