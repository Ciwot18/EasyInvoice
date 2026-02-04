package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
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
class QuoteRepositoryTests {

    @Autowired
    private QuoteRepository quoteRepository;

    @Test
    void testFindByIdAndCompanyId() {
        assertThat(quoteRepository.findByIdAndCompanyId(1000L, 2L)).isPresent();
        assertThat(quoteRepository.findByIdAndCompanyId(1000L, 3L)).isNotPresent();
    }

    @Test
    void testFindByCompanyIdAndStatusWithPaging() {
        Page<Quote> page = quoteRepository.findByCompanyIdAndStatus(
                2L,
                QuoteStatus.DRAFT,
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent()).extracting(Quote::getTitle).contains("Preventivo Base");
    }

    @Test
    void testFindByCompanyIdAndCustomerIdOrderByIssueDateDesc() {
        assertThat(quoteRepository.findByCompanyIdAndCustomerIdOrderByIssueDateDesc(2L, 100L))
                .extracting(Quote::getTitle)
                .containsExactly("Preventivo Inviato", "Preventivo Base");
    }

    @Test
    void testFindMaxQuoteNumber() {
        assertThat(quoteRepository.findMaxQuoteNumber(2L, 2025)).isEqualTo(12);
    }

    @Test
    void testSearchByCompanyId() {
        Page<Quote> byTitle = quoteRepository.searchByCompanyId(
                2L,
                "accettato",
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );
        assertThat(byTitle.getTotalElements()).isEqualTo(1);
        assertThat(byTitle.getContent()).extracting(Quote::getTitle).containsExactly("Preventivo Accettato");

        Page<Quote> all = quoteRepository.searchByCompanyId(
                2L,
                null,
                PageRequest.of(0, 10, Sort.by("issueDate"))
        );
        assertThat(all.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}
