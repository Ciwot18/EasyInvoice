package com.kernith.easyinvoice.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvoiceTests {

    @Test
    void invoiceDefaultsAndSettersWork() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);

        assertEquals(company, invoice.getCompany());
        assertEquals(customer, invoice.getCustomer());
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
        assertEquals("EUR", invoice.getCurrency());
        assertNotNull(invoice.getIssueDate());
        assertNotNull(invoice.getItems());
        assertTrue(invoice.getItems().isEmpty());
        assertAmount(BigDecimal.ZERO, invoice.getTotalAmount());

        invoice.setInvoiceYear(2025);
        invoice.setInvoiceNumber(77);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setTitle("Title");
        invoice.setNotes("Notes");
        invoice.setIssueDate(LocalDate.of(2025, 1, 10));
        invoice.setDueDate(LocalDate.of(2025, 2, 10));
        invoice.setCurrency("USD");

        assertEquals(2025, invoice.getInvoiceYear());
        assertEquals(77, invoice.getInvoiceNumber());
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
        assertEquals("Title", invoice.getTitle());
        assertEquals("Notes", invoice.getNotes());
        assertEquals(LocalDate.of(2025, 1, 10), invoice.getIssueDate());
        assertEquals(LocalDate.of(2025, 2, 10), invoice.getDueDate());
        assertEquals("USD", invoice.getCurrency());
    }

    @Test
    void constructorFromQuoteCopiesFieldsAndTotals() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        quote.setTitle("Quote title");
        quote.setNotes("Quote notes");
        quote.setCurrency("USD");
        quote.setIssueDate(LocalDate.of(2025, 1, 5));

        QuoteItem item1 = new QuoteItem(
                quote,
                1,
                "Item 1",
                new BigDecimal("2"),
                new BigDecimal("10"),
                new BigDecimal("10"),
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        QuoteItem item2 = new QuoteItem(
                quote,
                2,
                "Item 2",
                BigDecimal.ONE,
                new BigDecimal("5"),
                BigDecimal.ZERO,
                DiscountType.AMOUNT,
                new BigDecimal("2")
        );
        quote.getItems().add(item1);
        quote.getItems().add(item2);

        Invoice invoice = new Invoice(quote);

        assertEquals(company, invoice.getCompany());
        assertEquals(customer, invoice.getCustomer());
        assertEquals(quote, invoice.getSourceQuote());
        assertEquals("Quote title", invoice.getTitle());
        assertEquals("Quote notes", invoice.getNotes());
        assertEquals("USD", invoice.getCurrency());
        assertEquals(2, invoice.getItems().size());
        assertAmount(new BigDecimal("23"), invoice.getSubtotalAmount());
        assertAmount(new BigDecimal("2"), invoice.getTaxAmount());
        assertAmount(new BigDecimal("25"), invoice.getTotalAmount());
    }

    @Test
    void recalculateTotalsFromItemsAggregatesValuesAndSkipsNulls() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);

        InvoiceItem item1 = new InvoiceItem(
                invoice,
                1,
                "Item 1",
                new BigDecimal("2"),
                new BigDecimal("10"),
                new BigDecimal("10"),
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        InvoiceItem item2 = new InvoiceItem(
                invoice,
                2,
                "Item 2",
                BigDecimal.ONE,
                new BigDecimal("5"),
                BigDecimal.ZERO,
                DiscountType.AMOUNT,
                new BigDecimal("2")
        );

        invoice.getItems().add(item1);
        invoice.getItems().add(null);
        invoice.getItems().add(item2);

        invoice.recalculateTotalsFromItems(invoice.getItems());

        assertAmount(new BigDecimal("23"), invoice.getSubtotalAmount());
        assertAmount(new BigDecimal("2"), invoice.getTaxAmount());
        assertAmount(new BigDecimal("25"), invoice.getTotalAmount());
    }

    @Test
    void recalculateTotalsHandlesNullAmounts() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);

        InvoiceItem item = mock(InvoiceItem.class);
        when(item.getLineSubtotalAmount()).thenReturn(null);
        when(item.getLineTaxAmount()).thenReturn(null);
        when(item.getLineTotalAmount()).thenReturn(null);

        invoice.recalculateTotalsFromItems(List.of(item));

        assertAmount(BigDecimal.ZERO, invoice.getSubtotalAmount());
        assertAmount(BigDecimal.ZERO, invoice.getTaxAmount());
        assertAmount(BigDecimal.ZERO, invoice.getTotalAmount());
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
