package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.InvoiceItem;
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
class InvoiceItemRepositoryTests {

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Test
    void testFindByInvoiceIdOrderByPositionAsc() {
        assertThat(invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(2000L))
                .extracting(InvoiceItem::getDescription)
                .containsExactly("Analisi", "Sviluppo");
    }

    @Test
    void testFindByIdAndInvoiceId() {
        assertThat(invoiceItemRepository.findByIdAndInvoiceId(2102L, 2001L)).isPresent();
        assertThat(invoiceItemRepository.findByIdAndInvoiceId(2102L, 2000L)).isNotPresent();
    }
}
