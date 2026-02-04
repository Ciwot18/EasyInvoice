package com.kernith.easyinvoice.data.dto.invoice;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummaryResponse(
        Long id,
        Integer invoiceYear,
        Integer invoiceNumber,
        InvoiceStatus status,
        String title,
        LocalDate issueDate,
        LocalDate dueDate,
        String currency,
        BigDecimal totalAmount,
        Long customerId,
        String customerDisplayName
) {
    public static InvoiceSummaryResponse from(Invoice invoice) {
        return new InvoiceSummaryResponse(
                invoice.getId(),
                invoice.getInvoiceYear(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTitle(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                invoice.getCustomer() == null ? null : invoice.getCustomer().getId(),
                invoice.getCustomer() == null ? null : invoice.getCustomer().getDisplayName()
        );
    }
}
