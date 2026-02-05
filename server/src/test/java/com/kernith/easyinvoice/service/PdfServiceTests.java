package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfServiceTests {

    @Test
    void invoicePdfReturnsBytesWhenCompanyMatches() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        PdfService pdfService = new PdfService(
                invoiceRepository,
                invoiceItemRepository,
                quoteRepository,
                quoteItemRepository
        );

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 10L);
        company.setAddress("Via Roma 1");
        Customer customer = new Customer(company);
        customer.setAddress("Via Milano 2");
        Invoice invoice = new Invoice(company, customer);
        when(invoiceRepository.findById(77L)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(eq(77L))).thenReturn(List.of());

        AuthPrincipal principal = new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of());
        byte[] pdf = pdfService.invoicePdf(77L, principal);

        assertNotNull(pdf);
    }

    @Test
    void invoicePdfThrowsWhenCompanyMismatch() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        PdfService pdfService = new PdfService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(QuoteRepository.class),
                mock(QuoteItemRepository.class)
        );

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 10L);
        Invoice invoice = new Invoice(company, new Customer(company));
        when(invoiceRepository.findById(77L)).thenReturn(java.util.Optional.of(invoice));

        AuthPrincipal principal = new AuthPrincipal(1L, 99L, "COMPANY_MANAGER", List.of());
        assertThrows(ResponseStatusException.class, () -> pdfService.invoicePdf(77L, principal));
    }

    @Test
    void quotePdfReturnsBytesWhenCompanyMatches() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        PdfService pdfService = new PdfService(
                mock(InvoiceRepository.class),
                mock(InvoiceItemRepository.class),
                quoteRepository,
                quoteItemRepository
        );

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 10L);
        company.setAddress("Via Roma 1");
        Customer customer = new Customer(company);
        customer.setAddress("Via Milano 2");
        Quote quote = new Quote(company, customer);
        when(quoteRepository.findById(88L)).thenReturn(java.util.Optional.of(quote));
        when(quoteItemRepository.findByQuoteIdOrderByPositionAsc(eq(88L))).thenReturn(List.of());

        AuthPrincipal principal = new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of());
        byte[] pdf = pdfService.quotePdf(88L, principal);

        assertNotNull(pdf);
    }

    @Test
    void quotePdfThrowsWhenCompanyMismatch() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        PdfService pdfService = new PdfService(
                mock(InvoiceRepository.class),
                mock(InvoiceItemRepository.class),
                quoteRepository,
                mock(QuoteItemRepository.class)
        );

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 10L);
        Quote quote = new Quote(company, new Customer(company));
        when(quoteRepository.findById(88L)).thenReturn(java.util.Optional.of(quote));

        AuthPrincipal principal = new AuthPrincipal(1L, 99L, "COMPANY_MANAGER", List.of());
        assertThrows(ResponseStatusException.class, () -> pdfService.quotePdf(88L, principal));
    }
}
