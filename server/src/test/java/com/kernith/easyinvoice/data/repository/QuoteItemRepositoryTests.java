package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.QuoteItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class QuoteItemRepositoryTests {

    @Autowired
    private QuoteItemRepository quoteItemRepository;

    @Test
    void testFindByQuoteIdOrderByPositionAsc() {
        assertThat(quoteItemRepository.findByQuoteIdOrderByPositionAsc(1000L))
                .extracting(QuoteItem::getDescription)
                .containsExactly("Analisi", "Sviluppo");
    }

    @Test
    void testFindByIdAndQuoteId() {
        assertThat(quoteItemRepository.findByIdAndQuoteId(1102L, 1001L)).isPresent();
        assertThat(quoteItemRepository.findByIdAndQuoteId(1102L, 1000L)).isNotPresent();
    }
}
