package com.kernith.easyinvoice.data.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceItemTests {

    @Test
    void invoiceItemDefaultsAndRecalculationWork() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);
        InvoiceItem item = new InvoiceItem(invoice);

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
        Invoice invoice = new Invoice(company, customer);
        InvoiceItem item = new InvoiceItem(invoice);

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
        Invoice invoice = new Invoice(company, customer);
        InvoiceItem item = new InvoiceItem(invoice);

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
    void quoteConstructorMapsDiscountType() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem quoteItem = new QuoteItem(
                quote,
                1,
                "Item",
                BigDecimal.ONE,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                DiscountType.PERCENT,
                new BigDecimal("10")
        );

        Invoice invoice = new Invoice(company, customer);
        InvoiceItem item = new InvoiceItem(invoice, quoteItem);

        assertEquals(DiscountType.PERCENT, item.getDiscountType());
        assertAmount(new BigDecimal("9"), item.getLineSubtotalAmount());
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
