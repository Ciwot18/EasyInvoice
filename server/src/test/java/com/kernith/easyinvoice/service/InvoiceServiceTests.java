package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.CreateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoice.UpdateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceServiceTests {

    @Test
    void createInvoiceReturnsSavedInvoiceWhenValid() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceItemRepository,
                companyRepository,
                customerRepository,
                quoteRepository
        );

        Company company = new Company();
        Customer customer = new Customer(company);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(invoiceRepository.findMaxInvoiceNumber(10L, 2025)).thenReturn(3);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateInvoiceItemRequest itemReq = new CreateInvoiceItemRequest(
                1,
                " Service ",
                null,
                new BigDecimal("2"),
                null,
                new BigDecimal("10"),
                new BigDecimal("22"),
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        CreateInvoiceRequest req = new CreateInvoiceRequest(
                100L,
                null,
                " Title ",
                " Notes ",
                LocalDate.of(2025, 1, 10),
                null,
                " eur ",
                List.of(itemReq)
        );

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        Invoice created = invoiceService.createInvoice(req, principal);

        assertEquals("Title", created.getTitle());
        assertEquals("Notes", created.getNotes());
        assertEquals("EUR", created.getCurrency());
        assertEquals(2025, created.getInvoiceYear());
        assertEquals(4, created.getInvoiceNumber());
        verify(invoiceItemRepository).saveAll(anyList());
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    void createInvoiceThrowsWhenCompanyMissing() {
        InvoiceService invoiceService = new InvoiceService(
                mock(InvoiceRepository.class),
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        CreateInvoiceRequest req = new CreateInvoiceRequest(
                100L,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> invoiceService.createInvoice(req, principal));
    }

    @Test
    void createInvoiceThrowsWhenItemsMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceItemRepository,
                companyRepository,
                customerRepository,
                quoteRepository
        );

        when(companyRepository.findById(10L)).thenReturn(Optional.of(new Company()));
        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(new Customer(new Company())));
        when(invoiceRepository.findMaxInvoiceNumber(10L, 2025)).thenReturn(0);

        CreateInvoiceRequest req = new CreateInvoiceRequest(
                100L,
                null,
                null,
                null,
                LocalDate.of(2025, 1, 10),
                null,
                null,
                List.of()
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertThrows(ResponseStatusException.class, () -> invoiceService.createInvoice(req, principal));
    }

    @Test
    void createInvoiceFromQuoteReturnsInvoiceWithItems() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceItemRepository,
                companyRepository,
                customerRepository,
                quoteRepository
        );

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
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        quote.getItems().add(quoteItem);

        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(invoiceRepository.findMaxInvoiceNumber(10L, LocalDate.now().getYear())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        Invoice created = invoiceService.createInvoiceFromQuote(77L, principal);

        assertEquals(3, created.getInvoiceNumber());
        assertEquals(InvoiceStatus.DRAFT, created.getStatus());
        assertEquals(quote, created.getSourceQuote());
        assertEquals(1, created.getItems().size());
        verify(invoiceItemRepository).saveAll(anyList());
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    void createInvoiceFromQuoteThrowsWhenMissing() {
        InvoiceService invoiceService = new InvoiceService(
                mock(InvoiceRepository.class),
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> invoiceService.createInvoiceFromQuote(77L, principal));
    }

    @Test
    void updateInvoiceThrowsWhenMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateInvoiceRequest req = new UpdateInvoiceRequest("Title", null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> invoiceService.updateInvoice(10L, req, principal));
    }

    @Test
    void updateInvoiceThrowsWhenNotEditable() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(invoice));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateInvoiceRequest req = new UpdateInvoiceRequest("Title", null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> invoiceService.updateInvoice(10L, req, principal));
    }

    @Test
    void updateInvoiceUpdatesFields() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateInvoiceRequest req = new UpdateInvoiceRequest(
                " New ",
                " Notes ",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 3, 1),
                " usd "
        );

        Invoice updated = invoiceService.updateInvoice(10L, req, principal);

        assertEquals("New", updated.getTitle());
        assertEquals("Notes", updated.getNotes());
        assertEquals("USD", updated.getCurrency());
    }

    @Test
    void listInvoicesUsesSearchWhenQueryProvided() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Page<Invoice> page = new PageImpl<>(List.of(new Invoice(new Company(), new Customer(new Company()))));
        when(invoiceRepository.searchByCompanyId(eq(10L), eq("acme"), any(Pageable.class))).thenReturn(page);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Invoice> result = invoiceService.listInvoices(principal, 0, 20, "issueDate,desc", " acme ");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listInvoicesUsesFindWhenQueryBlank() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Page<Invoice> page = new PageImpl<>(List.of());
        when(invoiceRepository.findByCompanyId(eq(10L), any(Pageable.class))).thenReturn(page);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Invoice> result = invoiceService.listInvoices(principal, -1, 0, "invoiceNumber,asc", " ");

        assertTrue(result.isEmpty());
        verify(invoiceRepository).findByCompanyId(eq(10L), any(Pageable.class));
    }

    @Test
    void transitionThrowsWhenInvalidStatus() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(invoice));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertThrows(ResponseStatusException.class, () -> invoiceService.payInvoice(10L, principal));
    }

    @Test
    void transitionReturnsFalseWhenMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertTrue(Boolean.FALSE.equals(invoiceService.issueInvoice(10L, principal)));
    }

    @Test
    void transitionUpdatesStatusWhenValid() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                mock(InvoiceItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class),
                mock(QuoteRepository.class)
        );
        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertTrue(Boolean.TRUE.equals(invoiceService.issueInvoice(10L, principal)));
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }
}
