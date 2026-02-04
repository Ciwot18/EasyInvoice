package com.kernith.easyinvoice.data.dto.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceDetailResponse(
        Long id,
        Integer invoiceYear,
        Integer invoiceNumber,
        InvoiceStatus status,
        String title,
        String notes,
        LocalDate issueDate,
        LocalDate dueDate,
        String currency,
        BigDecimal subtotalAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Long customerId,
        String customerDisplayName,
        Long sourceQuoteId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InvoiceDetailResponse from(Invoice invoice) {
        return new InvoiceDetailResponse(
                invoice.getId(),
                invoice.getInvoiceYear(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTitle(),
                invoice.getNotes(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getCurrency(),
                invoice.getSubtotalAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getCustomer() == null ? null : invoice.getCustomer().getId(),
                invoice.getCustomer() == null ? null : invoice.getCustomer().getDisplayName(),
                invoice.getSourceQuote() == null ? null : invoice.getSourceQuote().getId(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
