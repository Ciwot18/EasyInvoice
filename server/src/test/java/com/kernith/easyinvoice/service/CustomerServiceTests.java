package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.customer.CreateCustomerRequest;
import com.kernith.easyinvoice.data.dto.customer.UpdateCustomerRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
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
}