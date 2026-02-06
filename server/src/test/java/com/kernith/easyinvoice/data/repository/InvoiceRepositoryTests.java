package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class InvoiceRepositoryTests {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void testFindByIdAndCompanyId() {
        assertThat(invoiceRepository.findByIdAndCompanyId(2000L, 2L)).isPresent();
        assertThat(invoiceRepository.findByIdAndCompanyId(2000L, 3L)).isNotPresent();
    }

    @Test
    void testFindByCompanyIdAndStatusWithPaging() {
        Page<Invoice> page = invoiceRepository.findByCompanyIdAndStatus(
                2L,
                InvoiceStatus.DRAFT,
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent()).extracting(Invoice::getTitle).contains("Fattura Bozza");
    }

    @Test
    void testFindByCompanyIdAndCustomerIdOrderByIssueDateDesc() {
        assertThat(invoiceRepository.findByCompanyIdAndCustomerIdOrderByIssueDateDesc(2L, 100L))
                .extracting(Invoice::getTitle)
                .containsExactly("Fattura Emessa", "Fattura Bozza");
    }

    @Test
    void testFindMaxInvoiceNumber() {
        assertThat(invoiceRepository.findMaxInvoiceNumber(2L, 2025)).isEqualTo(22);
    }

    @Test
    void testSearchByCompanyId() {
        Page<Invoice> byTitle = invoiceRepository.searchByCompanyId(
                2L,
                "emessa",
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );
        assertThat(byTitle.getTotalElements()).isEqualTo(1);
        assertThat(byTitle.getContent()).extracting(Invoice::getTitle).containsExactly("Fattura Emessa");

        Page<Invoice> all = invoiceRepository.searchByCompanyId(
                2L,
                null,
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );
        assertThat(all.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testAggregateByStatus() {
        List<InvoiceStatusAggregate> aggregates = invoiceRepository.aggregateByStatus(2L);

        assertThat(aggregates).isNotEmpty();
        InvoiceStatusAggregate issued = aggregates.stream()
                .filter(row -> row.getStatus() == InvoiceStatus.ISSUED)
                .findFirst()
                .orElseThrow();
        InvoiceStatusAggregate paid = aggregates.stream()
                .filter(row -> row.getStatus() == InvoiceStatus.PAID)
                .findFirst()
                .orElseThrow();

        assertThat(issued.getCount()).isEqualTo(1L);
        assertThat(issued.getTotalAmount()).isEqualByComparingTo(new BigDecimal("183.00"));
        assertThat(paid.getCount()).isEqualTo(1L);
        assertThat(paid.getTotalAmount()).isEqualByComparingTo(new BigDecimal("244.00"));
    }

    @Test
    void testAggregateByStatusForCustomer() {
        List<InvoiceStatusAggregate> aggregates = invoiceRepository.aggregateByStatusForCustomer(2L, 100L);

        assertThat(aggregates).isNotEmpty();
        InvoiceStatusAggregate draft = aggregates.stream()
                .filter(row -> row.getStatus() == InvoiceStatus.DRAFT)
                .findFirst()
                .orElseThrow();
        InvoiceStatusAggregate issued = aggregates.stream()
                .filter(row -> row.getStatus() == InvoiceStatus.ISSUED)
                .findFirst()
                .orElseThrow();

        assertThat(draft.getCount()).isEqualTo(1L);
        assertThat(draft.getTotalAmount()).isEqualByComparingTo(new BigDecimal("122.00"));
        assertThat(issued.getCount()).isEqualTo(1L);
        assertThat(issued.getTotalAmount()).isEqualByComparingTo(new BigDecimal("183.00"));
    }
}
