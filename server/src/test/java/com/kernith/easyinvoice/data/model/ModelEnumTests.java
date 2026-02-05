package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelEnumTests {

    @Test
    void invoiceStatusEnumHasExpectedValues() {
        assertNotNull(InvoiceStatus.valueOf("DRAFT"));
        assertNotNull(InvoiceStatus.valueOf("ISSUED"));
        assertNotNull(InvoiceStatus.valueOf("PAID"));
        assertNotNull(InvoiceStatus.valueOf("OVERDUE"));
        assertNotNull(InvoiceStatus.valueOf("ARCHIVED"));
    }

    @Test
    void quoteStatusEnumHasExpectedValues() {
        assertNotNull(QuoteStatus.valueOf("DRAFT"));
        assertNotNull(QuoteStatus.valueOf("SENT"));
        assertNotNull(QuoteStatus.valueOf("ACCEPTED"));
        assertNotNull(QuoteStatus.valueOf("REJECTED"));
        assertNotNull(QuoteStatus.valueOf("EXPIRED"));
        assertNotNull(QuoteStatus.valueOf("CONVERTED"));
        assertNotNull(QuoteStatus.valueOf("ARCHIVED"));
    }

    @Test
    void discountTypeEnumHasExpectedValues() {
        assertNotNull(DiscountType.valueOf("NONE"));
        assertNotNull(DiscountType.valueOf("PERCENT"));
        assertNotNull(DiscountType.valueOf("AMOUNT"));
    }
}
