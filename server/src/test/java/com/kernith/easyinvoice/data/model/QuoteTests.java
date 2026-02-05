package com.kernith.easyinvoice.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuoteTests {

    @Test
    void quoteDefaultsAndSettersWork() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);

        assertEquals(company, quote.getCompany());
        assertEquals(customer, quote.getCustomer());
        assertEquals(QuoteStatus.DRAFT, quote.getStatus());
        assertEquals("EUR", quote.getCurrency());
        assertNotNull(quote.getIssueDate());
        assertNotNull(quote.getItems());
        assertTrue(quote.getItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(quote.getTotalAmount()));
        assertNull(quote.getId());
        assertNull(quote.getCreatedAt());
        assertNull(quote.getUpdatedAt());

        quote.setQuoteYear(2025);
        quote.setQuoteNumber(77);
        quote.setStatus(QuoteStatus.SENT);
        quote.setTitle("Title");
        quote.setNotes("Notes");
        quote.setIssueDate(LocalDate.of(2025, 1, 10));
        quote.setValidUntil(LocalDate.of(2025, 2, 10));
        quote.setCurrency("USD");

        assertEquals(2025, quote.getQuoteYear());
        assertEquals(77, quote.getQuoteNumber());
        assertEquals(QuoteStatus.SENT, quote.getStatus());
        assertEquals("Title", quote.getTitle());
        assertEquals("Notes", quote.getNotes());
        assertEquals(LocalDate.of(2025, 1, 10), quote.getIssueDate());
        assertEquals(LocalDate.of(2025, 2, 10), quote.getValidUntil());
        assertEquals("USD", quote.getCurrency());
    }

    @Test
    void recalculateTotalsFromItemsAggregatesValuesAndSkipsNulls() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);

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
        quote.getItems().add(null);
        quote.getItems().add(item2);

        quote.recalculateTotalsFromItems(quote.getItems());

        assertAmount(new BigDecimal("23"), quote.getSubtotalAmount());
        assertAmount(new BigDecimal("2"), quote.getTaxAmount());
        assertAmount(new BigDecimal("25"), quote.getTotalAmount());
    }

    @Test
    void recalculateTotalsHandlesNullAmounts() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);

        QuoteItem item = mock(QuoteItem.class);
        when(item.getLineSubtotalAmount()).thenReturn(null);
        when(item.getLineTaxAmount()).thenReturn(null);
        when(item.getLineTotalAmount()).thenReturn(null);

        quote.recalculateTotalsFromItems(List.of(item));

        assertAmount(BigDecimal.ZERO, quote.getSubtotalAmount());
        assertAmount(BigDecimal.ZERO, quote.getTaxAmount());
        assertAmount(BigDecimal.ZERO, quote.getTotalAmount());
    }

    @Test
    void quoteStateMethodsUseFactoryAndUpdateState() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);

        quote.reject();
        assertEquals(QuoteStatus.REJECTED, quote.getStatus());

        quote.setStatus(QuoteStatus.SENT);
        assertEquals(QuoteStatus.SENT, quote.getStatus());
        quote.expire();
        assertEquals(QuoteStatus.EXPIRED, quote.getStatus());

        quote.setStatus(QuoteStatus.DRAFT);
        quote.send();
        assertEquals(QuoteStatus.SENT, quote.getStatus());

        quote.accept();
        assertEquals(QuoteStatus.ACCEPTED, quote.getStatus());

        quote.convert();
        assertEquals(QuoteStatus.CONVERTED, quote.getStatus());

        quote.archive();
        assertEquals(QuoteStatus.ARCHIVED, quote.getStatus());
    }

    @Test
    void initStateUsesCurrentStatusWhenAlreadySet() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        quote.setStatus(QuoteStatus.SENT);

        ReflectionTestUtils.invokeMethod(quote, "initState");
        quote.accept();

        assertEquals(QuoteStatus.ACCEPTED, quote.getStatus());
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
