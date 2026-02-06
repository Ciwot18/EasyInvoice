package com.kernith.easyinvoice.data.dto.dashboard;

import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;

public record QuoteStatusAggregateResponse(
        QuoteStatus status,
        long count,
        BigDecimal totalAmount
) {}
