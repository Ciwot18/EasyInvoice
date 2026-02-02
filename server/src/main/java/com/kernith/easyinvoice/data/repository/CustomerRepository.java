package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Customer;
import java.util.List;
import java.util.Optional;

import com.kernith.easyinvoice.data.model.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCompanyIdAndStatusOrderByDisplayNameAsc(Long companyId, CustomerStatus status);

    Page<Customer> findByCompanyIdAndStatus(Long companyId, Pageable pageable, CustomerStatus status);

    Optional<Customer> findByIdAndCompanyIdAndStatus(Long id, Long companyId, CustomerStatus Status);

    Optional<Customer> findByCompanyIdAndVatNumberAndStatus(Long companyId, String vatNumber, CustomerStatus status);

    @Query("""
            select c
            from Customer c
            where c.company.id = :companyId
              and (
                :q is null or :q = ''
                or lower(c.displayName) like lower(concat('%', :q, '%'))
                or lower(c.legalName) like lower(concat('%', :q, '%'))
                or lower(c.email) like lower(concat('%', :q, '%'))
                or c.vatNumber like concat('%', :q, '%')
                or c.country like concat('%', :q, '%')
              )
            """)
    Page<Customer> searchByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("q") String q, Pageable pageable, CustomerStatus status);
}
