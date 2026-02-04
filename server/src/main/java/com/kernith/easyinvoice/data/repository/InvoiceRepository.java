package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByIdAndCompanyId(Long id, Long companyId);

    Page<Invoice> findByCompanyId(Long companyId, Pageable pageable);

    Page<Invoice> findByCompanyIdAndStatus(Long companyId, InvoiceStatus status, Pageable pageable);

    java.util.List<Invoice> findByCompanyIdAndCustomerIdOrderByIssueDateDesc(Long companyId, Long customerId);

    @Query("""
            select coalesce(max(i.invoiceNumber), 0)
            from Invoice i
            where i.company.id = :companyId
              and i.invoiceYear = :invoiceYear
            """)
    Integer findMaxInvoiceNumber(@Param("companyId") Long companyId, @Param("invoiceYear") Integer invoiceYear);

    @Query("""
            select i
            from Invoice i
            left join i.customer c
            where i.company.id = :companyId
              and (
                :q is null or :q = ''
                or lower(i.title) like lower(concat('%', :q, '%'))
                or lower(i.notes) like lower(concat('%', :q, '%'))
                or lower(c.displayName) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Invoice> searchByCompanyId(@Param("companyId") Long companyId, @Param("q") String q, Pageable pageable);
}