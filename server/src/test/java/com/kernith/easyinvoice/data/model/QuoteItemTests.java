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

        item.setPosition(1);
        item.setDescription("Consulting");
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("10"));
        item.setTaxRate(new BigDecimal("10"));
        item.setDiscountType(DiscountType.NONE);

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

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
