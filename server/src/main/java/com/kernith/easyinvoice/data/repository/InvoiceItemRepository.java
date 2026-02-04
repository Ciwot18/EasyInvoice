package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.InvoiceItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceIdOrderByPositionAsc(Long invoiceId);

    Optional<InvoiceItem> findByIdAndInvoiceId(Long id, Long invoiceId);
}