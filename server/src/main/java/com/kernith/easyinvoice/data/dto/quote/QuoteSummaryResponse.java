package com.kernith.easyinvoice.data.dto.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteSummaryResponse(
        Long id,
        Integer quoteYear,
        Integer quoteNumber,
        QuoteStatus status,
        String title,
        LocalDate issueDate,
        LocalDate validUntil,
        String currency,
        BigDecimal totalAmount,
        Long customerId,
        String customerDisplayName
) {
    public static QuoteSummaryResponse from(Quote quote) {
        return new QuoteSummaryResponse(
                quote.getId(),
                quote.getQuoteYear(),
                quote.getQuoteNumber(),
                quote.getStatus(),
                quote.getTitle(),
                quote.getIssueDate(),
                quote.getValidUntil(),
                quote.getCurrency(),
                quote.getTotalAmount(),
                quote.getCustomer() == null ? null : quote.getCustomer().getId(),
                quote.getCustomer() == null ? null : quote.getCustomer().getDisplayName()
        );
    }
}
