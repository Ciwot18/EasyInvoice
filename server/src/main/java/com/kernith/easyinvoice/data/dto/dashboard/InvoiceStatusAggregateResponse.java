package com.kernith.easyinvoice.data.dto.dashboard;

import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;

public record InvoiceStatusAggregateResponse(
        InvoiceStatus status,
        long count,
        BigDecimal totalAmount
) {}
