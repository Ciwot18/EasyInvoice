package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.InvoicePdfArchive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoicePdfArchiveRepository extends JpaRepository<InvoicePdfArchive, Long> {
    List<InvoicePdfArchive> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    Optional<InvoicePdfArchive> findFirstByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    Optional<InvoicePdfArchive> findByIdAndInvoiceId(Long id, Long invoiceId);
}