package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.customer.CreateCustomerRequest;
import com.kernith.easyinvoice.data.dto.customer.UpdateCustomerRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;

import com.kernith.easyinvoice.data.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceTests {

    @Test
    void createCustomerReturnsSavedCustomerWhenValid() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        Company company = new Company();
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(customerRepository.findByCompanyIdAndVatNumberAndStatus(eq(10L), eq("IT123"), eq(CustomerStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        CreateCustomerRequest req = new CreateCustomerRequest(
                " Acme Spa ",
                " Acme Spa ",
                " IT123 ",
                "info@acme.test",
                "123",
                "pec@acme.test",
                "Via Roma 1",
                "Roma",
                "00100",
                "",
                null
        );

        Customer created = customerService.createCustomer(req, principal);

        assertEquals("Acme Spa", created.getDisplayName());
        assertEquals("IT123", created.getVatNumber());
        assertEquals("IT", created.getCountry());
        assertEquals(CustomerStatus.ACTIVE, created.getStatus());
    }

    @Test
    void updateCustomerThrowsWhenCustomerMissing() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateCustomerRequest req = new UpdateCustomerRequest(
                "Acme Spa",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ResponseStatusException.class,
                () -> customerService.updateCustomer(10L, req, principal));
    }

    @Test
    void createCustomerThrowsWhenVatAlreadyUsed() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByCompanyIdAndVatNumberAndStatus(eq(10L), eq("IT123"), eq(CustomerStatus.ACTIVE)))
                .thenReturn(Optional.of(new Customer(new Company())));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        CreateCustomerRequest req = new CreateCustomerRequest(
                "Acme Spa",
                "Acme Spa",
                "IT123",
                "info@acme.test",
                "123",
                "pec@acme.test",
                "Via Roma 1",
                "Roma",
                "00100",
                "IT",
                CustomerStatus.ACTIVE
        );

        assertThrows(ResponseStatusException.class,
                () -> customerService.createCustomer(req, principal));
    }

    @Test
    void listCustomersUsesSearchWhenQueryProvided() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Customer> page = new PageImpl<>(List.of(new Customer(new Company())));
        when(customerRepository.searchByCompanyIdAndStatus(eq(10L), eq(CustomerStatus.ACTIVE), eq("acme"), any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = customerService.listCustomers(principal, 0, 20, "displayName,asc", " acme ");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listCustomersUsesFindWhenQueryBlank() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        Page<Customer> page = new PageImpl<>(List.of());
        when(customerRepository.findByCompanyIdAndStatus(eq(10L), eq(CustomerStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = customerService.listCustomers(principal, -1, 0, "vatNumber,desc", " ");

        assertTrue(result.isEmpty());
        verify(customerRepository).findByCompanyIdAndStatus(eq(10L), eq(CustomerStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void updateCustomerAllowsSameVatNumber() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        Customer customer = new Customer(new Company());
        customer.setVatNumber("IT123");
        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateCustomerRequest req = new UpdateCustomerRequest(
                null,
                null,
                "IT123",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Customer updated = customerService.updateCustomer(10L, req, principal);

        assertEquals("IT123", updated.getVatNumber());
    }

    @Test
    void deleteCustomerReturnsEmptyWhenMissing() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        assertTrue(customerService.deleteCustomer(10L, principal).isEmpty());
    }

    @Test
    void createCustomerThrowsWhenCompanyMissing() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByCompanyIdAndVatNumberAndStatus(eq(10L), eq("IT123"), eq(CustomerStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        CreateCustomerRequest req = new CreateCustomerRequest(
                "Acme Spa",
                "Acme Spa",
                "IT123",
                "info@acme.test",
                "123",
                "pec@acme.test",
                "Via Roma 1",
                "Roma",
                "00100",
                "IT",
                CustomerStatus.ACTIVE
        );

        assertThrows(ResponseStatusException.class, () -> customerService.createCustomer(req, principal));
    }

    @Test
    void updateCustomerUpdatesFieldsAndNormalizesValues() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        Customer customer = new Customer(new Company());
        customer.setVatNumber("IT123");
        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateCustomerRequest req = new UpdateCustomerRequest(
                " New Name ",
                " Legal ",
                " IT123 ",
                " INFO@ACME.TEST ",
                " 123 ",
                " PEC@ACME.TEST ",
                " Via Roma 2 ",
                " Roma ",
                " 00100 ",
                " it "
        );

        Customer updated = customerService.updateCustomer(10L, req, principal);

        assertEquals("New Name", updated.getDisplayName());
        assertEquals("Legal", updated.getLegalName());
        assertEquals("it123".toUpperCase(), updated.getVatNumber());
        assertEquals("info@acme.test", updated.getEmail());
        assertEquals("pec@acme.test", updated.getPec());
        assertEquals("Via Roma 2", updated.getAddress());
        assertEquals("Roma", updated.getCity());
        assertEquals("00100", updated.getPostalCode());
        assertEquals("IT", updated.getCountry());
    }

    @Test
    void updateCustomerThrowsWhenVatAlreadyUsed() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        Customer customer = new Customer(new Company());
        customer.setVatNumber("IT123");
        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(customerRepository.findByCompanyIdAndVatNumberAndStatus(eq(10L), eq("IT999"), eq(CustomerStatus.ACTIVE)))
                .thenReturn(Optional.of(new Customer(new Company())));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        UpdateCustomerRequest req = new UpdateCustomerRequest(
                null,
                null,
                "IT999",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ResponseStatusException.class, () -> customerService.updateCustomer(10L, req, principal));
    }

    @Test
    void archiveRestoreDeleteCustomerSetStatus() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        Customer customer = new Customer(new Company());
        when(customerRepository.findByIdAndCompanyIdAndStatus(10L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertTrue(customerService.archiveCustomer(10L, principal).isPresent());
        assertEquals(CustomerStatus.ARCHIVED, customer.getStatus());

        customer.setStatus(CustomerStatus.ACTIVE);
        assertTrue(customerService.deleteCustomer(10L, principal).isPresent());
        assertEquals(CustomerStatus.DELETED, customer.getStatus());

        customer.setStatus(CustomerStatus.ARCHIVED);
        assertTrue(customerService.restoreCustomer(10L, principal).isPresent());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    void listCustomerQuotesReturnsQuotesWhenCustomerExists() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.of(new Customer(new Company())));
        when(quoteRepository.findByCompanyIdAndCustomerIdOrderByIssueDateDesc(10L, 100L))
                .thenReturn(List.of(new Quote()));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        List<Quote> quotes = customerService.listCustomerQuotes(100L, principal);

        assertEquals(1, quotes.size());
    }

    @Test
    void listCustomerQuotesThrowsWhenCustomerMissing() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        CustomerService customerService = new CustomerService(customerRepository, companyRepository, quoteRepository);

        when(customerRepository.findByIdAndCompanyIdAndStatus(100L, 10L, CustomerStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertThrows(ResponseStatusException.class, () -> customerService.listCustomerQuotes(100L, principal));
    }
}
