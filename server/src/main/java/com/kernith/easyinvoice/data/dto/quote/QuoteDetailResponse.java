package com.kernith.easyinvoice.data.dto.quote;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record QuoteDetailResponse(
        Long id,
        Integer quoteYear,
        Integer quoteNumber,
        QuoteStatus status,
        String title,
        String notes,
        LocalDate issueDate,
        LocalDate validUntil,
        String currency,
        BigDecimal subtotalAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Long customerId,
        String customerDisplayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuoteDetailResponse from(Quote quote) {
        return new QuoteDetailResponse(
                quote.getId(),
                quote.getQuoteYear(),
                quote.getQuoteNumber(),
                quote.getStatus(),
                quote.getTitle(),
                quote.getNotes(),
                quote.getIssueDate(),
                quote.getValidUntil(),
                quote.getCurrency(),
                quote.getSubtotalAmount(),
                quote.getTaxAmount(),
                quote.getTotalAmount(),
                quote.getCustomer() == null ? null : quote.getCustomer().getId(),
                quote.getCustomer() == null ? null : quote.getCustomer().getDisplayName(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}
