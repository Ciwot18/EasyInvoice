package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByIdAndCompanyId(Long id, Long companyId);

    Page<Quote> findByCompanyId(Long companyId, Pageable pageable);

    Page<Quote> findByCompanyIdAndStatus(Long companyId, QuoteStatus status, Pageable pageable);

    java.util.List<Quote> findByCompanyIdAndCustomerIdOrderByIssueDateDesc(Long companyId, Long customerId);

    @Query("""
            select q.status as status,
                   count(q) as count,
                   coalesce(sum(q.totalAmount), 0) as totalAmount
            from Quote q
            where q.company.id = :companyId
            group by q.status
            """)
    List<QuoteStatusAggregate> aggregateByStatus(@Param("companyId") Long companyId);

    @Query("""
            select coalesce(max(q.quoteNumber), 0)
            from Quote q
            where q.company.id = :companyId
              and q.quoteYear = :quoteYear
            """)
    Integer findMaxQuoteNumber(@Param("companyId") Long companyId, @Param("quoteYear") Integer quoteYear);

    @Query("""
            select q
            from Quote q
            left join q.customer c
            where q.company.id = :companyId
              and (
                :q is null or :q = ''
                or lower(q.title) like lower(concat('%', :q, '%'))
                or lower(q.notes) like lower(concat('%', :q, '%'))
                or lower(c.displayName) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Quote> searchByCompanyId(@Param("companyId") Long companyId, @Param("q") String q, Pageable pageable);
}
