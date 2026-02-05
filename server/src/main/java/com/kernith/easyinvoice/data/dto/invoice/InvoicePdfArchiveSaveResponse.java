package com.kernith.easyinvoice.data.dto.invoice;

import com.kernith.easyinvoice.data.model.InvoicePdfArchive;

import java.time.LocalDateTime;

public record InvoicePdfArchiveSaveResponse(
        Long saveId,
        Long invoiceId,
        LocalDateTime createdAt
) {
    public static InvoicePdfArchiveSaveResponse from(InvoicePdfArchive invoicePdfArchive) {
        return new InvoicePdfArchiveSaveResponse(
                invoicePdfArchive.getId(),
                invoicePdfArchive.getInvoice().getId(),
                invoicePdfArchive.getCreatedAt()
        );
    }
}