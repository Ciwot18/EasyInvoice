package com.kernith.easyinvoice.helper.adapter;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfAdapterTests {

    @Test
    void quoteLineAdapterFormatsLabels() {
        Quote quote = new Quote(new Company(), new Customer(new Company()));
        QuoteItem item = new QuoteItem(
                quote,
                1,
                "Item",
                new BigDecimal("2"),
                new BigDecimal("10.00"),
                new BigDecimal("22"),
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        QuoteLineAdapter adapter = new QuoteLineAdapter(item, "EUR");

        assertEquals(1, adapter.position());
        assertEquals("Item", adapter.description());
        assertEquals("", adapter.notes());
        assertEquals("2.0000", adapter.qtyLabel());
        assertEquals("", adapter.unitLabel());
        assertEquals("€ 10.0000", adapter.unitPriceLabel());
        assertEquals("22.00%", adapter.taxRateLabel());
        assertTrue(adapter.lineTotalLabel().startsWith("€ "));
    }

    @Test
    void invoiceLineAdapterFormatsLabels() {
        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        InvoiceItem item = new InvoiceItem(
                invoice,
                1,
                "Item",
                new BigDecimal("2"),
                new BigDecimal("10.00"),
                new BigDecimal("22"),
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        InvoiceLineAdapter adapter = new InvoiceLineAdapter(item, "EUR");

        assertEquals(1, adapter.position());
        assertEquals("Item", adapter.description());
        assertEquals("", adapter.notes());
        assertEquals("2.0000", adapter.qtyLabel());
        assertEquals("", adapter.unitLabel());
        assertEquals("€ 10.0000", adapter.unitPriceLabel());
        assertEquals("22.00%", adapter.taxRateLabel());
        assertTrue(adapter.lineTotalLabel().startsWith("€ "));
    }

    @Test
    void quotePdfAdapterMapsFields() {
        Company company = new Company();
        company.setName("Acme");
        Customer customer = new Customer(company);
        customer.setLegalName("Acme Spa");

        Quote quote = new Quote(company, customer);
        quote.setQuoteYear(2025);
        quote.setQuoteNumber(10);
        quote.setStatus(QuoteStatus.SENT);
        quote.setIssueDate(LocalDate.of(2025, 1, 5));
        quote.setValidUntil(LocalDate.of(2025, 2, 5));
        quote.setCurrency("EUR");
        quote.setNotes(null);

        QuoteItem item = new QuoteItem(
                quote,
                1,
                "Item",
                BigDecimal.ONE,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        quote.getItems().add(item);

        QuotePdfAdapter adapter = new QuotePdfAdapter(quote, List.of(item), company, customer);

        assertEquals("Preventivo", adapter.title());
        assertEquals("2025/10", adapter.numberLabel());
        assertEquals("SENT", adapter.statusLabel());
        assertEquals("2025-01-05", adapter.issueDateLabel());
        assertEquals("2025-02-05", adapter.dueDateLabel());
        assertEquals("Acme", adapter.companyName());
        assertEquals("Acme Spa", adapter.customerName());
        assertEquals("EUR", adapter.currency());
        assertEquals("", adapter.notes());
        assertTrue(adapter.subtotalLabel().endsWith(" EUR"));
        assertTrue(adapter.taxLabel().endsWith(" EUR"));
        assertTrue(adapter.totalLabel().endsWith(" EUR"));
        assertFalse(adapter.lines().isEmpty());
    }

    @Test
    void invoicePdfAdapterMapsFields() {
        Company company = new Company();
        company.setName("Acme");
        Customer customer = new Customer(company);
        customer.setLegalName("Acme Spa");

        Invoice invoice = new Invoice(company, customer);
        invoice.setInvoiceYear(2025);
        invoice.setInvoiceNumber(20);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDate.of(2025, 1, 10));
        invoice.setDueDate(LocalDate.of(2025, 2, 10));
        invoice.setCurrency("EUR");
        invoice.setNotes("Note");

        InvoiceItem item = new InvoiceItem(
                invoice,
                1,
                "Item",
                BigDecimal.ONE,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );

        InvoicePdfAdapter adapter = new InvoicePdfAdapter(invoice, List.of(item), company, customer);

        assertEquals("Fattura", adapter.title());
        assertEquals("2025/20", adapter.numberLabel());
        assertEquals("DRAFT", adapter.statusLabel());
        assertEquals("2025-01-10", adapter.issueDateLabel());
        assertEquals("2025-02-10", adapter.dueDateLabel());
        assertEquals("Acme", adapter.companyName());
        assertEquals("Acme Spa", adapter.customerName());
        assertEquals("EUR", adapter.currency());
        assertEquals("Note", adapter.notes());
        assertEquals("€ 0.00", adapter.subtotalLabel());
        assertEquals("€ 0.00", adapter.taxLabel());
        assertEquals("€ 0.00", adapter.totalLabel());
        assertFalse(adapter.lines().isEmpty());
    }
}
