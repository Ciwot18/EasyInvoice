package com.kernith.easyinvoice.data.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuoteItemTests {

    @Test
    void quoteItemDefaultsAndRecalculationWork() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem item = new QuoteItem(quote);

        assertEquals(quote, item.getQuote());
        assertEquals(null, item.getId());
        assertEquals(null, item.getCreatedAt());
        assertEquals(null, item.getUpdatedAt());

        item.setPosition(1);
        item.setDescription("Consulting");
        item.setQuantity(new BigDecimal("2"));
        item.setNotes("Notes");
        item.setUnit("h");
        item.setUnitPrice(new BigDecimal("10"));
        item.setTaxRate(new BigDecimal("10"));
        item.setDiscountType(DiscountType.NONE);
        item.setDiscountValue(BigDecimal.ZERO);

        assertEquals(1, item.getPosition());
        assertEquals("Consulting", item.getDescription());
        assertEquals(new BigDecimal("2"), item.getQuantity());
        assertEquals("Notes", item.getNotes());
        assertEquals("h", item.getUnit());
        assertEquals(new BigDecimal("10"), item.getUnitPrice());
        assertEquals(new BigDecimal("10"), item.getTaxRate());
        assertEquals(DiscountType.NONE, item.getDiscountType());
        assertEquals(BigDecimal.ZERO, item.getDiscountValue());

        assertAmount(new BigDecimal("20"), item.getLineSubtotalAmount());
        assertAmount(new BigDecimal("2"), item.getLineTaxAmount());
        assertAmount(new BigDecimal("22"), item.getLineTotalAmount());
    }

    @Test
    void percentDiscountIsApplied() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem item = new QuoteItem(quote);

        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("10"));
        item.setTaxRate(new BigDecimal("10"));
        item.setDiscountType(DiscountType.PERCENT);
        item.setDiscountValue(new BigDecimal("50"));

        assertAmount(new BigDecimal("10"), item.getLineSubtotalAmount());
        assertAmount(new BigDecimal("1"), item.getLineTaxAmount());
        assertAmount(new BigDecimal("11"), item.getLineTotalAmount());
    }

    @Test
    void amountDiscountCannotGoBelowZero() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem item = new QuoteItem(quote);

        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(new BigDecimal("5"));
        item.setTaxRate(new BigDecimal("22"));
        item.setDiscountType(DiscountType.AMOUNT);
        item.setDiscountValue(new BigDecimal("10"));

        assertAmount(BigDecimal.ZERO, item.getLineSubtotalAmount());
        assertAmount(BigDecimal.ZERO, item.getLineTaxAmount());
        assertAmount(BigDecimal.ZERO, item.getLineTotalAmount());
    }

    @Test
    void nullDiscountTypeDefaultsToNone() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem item = new QuoteItem(quote);

        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("10"));
        item.setTaxRate(new BigDecimal("10"));
        item.setDiscountType(null);
        item.setDiscountValue(null);

        assertAmount(new BigDecimal("20"), item.getLineSubtotalAmount());
        assertAmount(new BigDecimal("2"), item.getLineTaxAmount());
        assertAmount(new BigDecimal("22"), item.getLineTotalAmount());
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
