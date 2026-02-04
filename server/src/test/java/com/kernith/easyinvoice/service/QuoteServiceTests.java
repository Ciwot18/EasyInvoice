package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quote.CreateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quote.UpdateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
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

class QuoteServiceTests {

    @Test
    void createQuoteReturnsSavedQuoteWhenValid() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        QuoteService quoteService = new QuoteService(quoteRepository, quoteItemRepository, companyRepository, customerRepository);

        Company company = new Company();
        Customer customer = new Customer(company);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(quoteRepository.findMaxQuoteNumber(10L, 2025)).thenReturn(3);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateQuoteItemRequest itemReq = new CreateQuoteItemRequest(
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
        CreateQuoteRequest req = new CreateQuoteRequest(
                100L,
                " Title ",
                " Notes ",
                LocalDate.of(2025, 1, 10),
                null,
                " eur ",
                List.of(itemReq)
        );

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        Quote created = quoteService.createQuote(req, principal);

        assertEquals("Title", created.getTitle());
        assertEquals("Notes", created.getNotes());
        assertEquals("EUR", created.getCurrency());
        assertEquals(2025, created.getQuoteYear());
        assertEquals(4, created.getQuoteNumber());
        verify(quoteItemRepository).saveAll(anyList());
        verify(quoteRepository, times(2)).save(any(Quote.class));
    }

    @Test
    void createQuoteThrowsWhenCompanyMissing() {
        QuoteService quoteService = new QuoteService(
                mock(QuoteRepository.class),
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        CreateQuoteRequest req = new CreateQuoteRequest(
                100L,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> quoteService.createQuote(req, principal));
    }

    @Test
    void createQuoteThrowsWhenItemsMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        QuoteService quoteService = new QuoteService(quoteRepository, quoteItemRepository, companyRepository, customerRepository);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(new Company()));
        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(new Customer(new Company())));
        when(quoteRepository.findMaxQuoteNumber(10L, 2025)).thenReturn(0);

        CreateQuoteRequest req = new CreateQuoteRequest(
                100L,
                null,
                null,
                LocalDate.of(2025, 1, 10),
                null,
                null,
                List.of()
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertThrows(ResponseStatusException.class, () -> quoteService.createQuote(req, principal));
    }

    @Test
    void updateQuoteThrowsWhenMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        when(quoteRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateQuoteRequest req = new UpdateQuoteRequest("Title", null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> quoteService.updateQuote(10L, req, principal));
    }

    @Test
    void updateQuoteUpdatesFields() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        Quote quote = new Quote(new Company(), new Customer(new Company()));
        when(quoteRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateQuoteRequest req = new UpdateQuoteRequest(
                " New ",
                " Notes ",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 3, 1),
                " usd "
        );

        Quote updated = quoteService.updateQuote(10L, req, principal);

        assertEquals("New", updated.getTitle());
        assertEquals("Notes", updated.getNotes());
        assertEquals("USD", updated.getCurrency());
    }

    @Test
    void listQuotesUsesSearchWhenQueryProvided() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        Page<Quote> page = new PageImpl<>(List.of(new Quote(new Company(), new Customer(new Company()))));
        when(quoteRepository.searchByCompanyId(eq(10L), eq("acme"), any(Pageable.class))).thenReturn(page);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Quote> result = quoteService.listQuotes(principal, 0, 20, "issueDate,desc", " acme ");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listQuotesUsesFindWhenQueryBlank() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        Page<Quote> page = new PageImpl<>(List.of());
        when(quoteRepository.findByCompanyId(eq(10L), any(Pageable.class))).thenReturn(page);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Quote> result = quoteService.listQuotes(principal, -1, 0, "quoteNumber,asc", " ");

        assertTrue(result.isEmpty());
        verify(quoteRepository).findByCompanyId(eq(10L), any(Pageable.class));
    }

    @Test
    void transitionThrowsWhenInvalidStatus() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.REJECTED);
        when(quoteRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.of(quote));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertThrows(ResponseStatusException.class, () -> quoteService.sendQuote(10L, principal));
    }

    @Test
    void transitionReturnsFalseWhenMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteService quoteService = new QuoteService(
                quoteRepository,
                mock(QuoteItemRepository.class),
                mock(CompanyRepository.class),
                mock(CustomerRepository.class)
        );
        when(quoteRepository.findByIdAndCompanyId(10L, 10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertTrue(Boolean.FALSE.equals(quoteService.sendQuote(10L, principal)));
    }
}
