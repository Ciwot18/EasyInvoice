package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.customer.CreateCustomerRequest;
import com.kernith.easyinvoice.data.dto.customer.UpdateCustomerRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.kernith.easyinvoice.helper.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final QuoteRepository quoteRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            QuoteRepository quoteRepository
    ) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.quoteRepository = quoteRepository;
    }

    public Customer createCustomer(CreateCustomerRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));

        Long companyId = getRequiredCompanyId(principal);
        String vatNumber = normalizeVat(request.vatNumber());

        customerRepository.findByCompanyIdAndVatNumberAndStatus(companyId, vatNumber, CustomerStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Vat number already used for this company");
                });

        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company specified is not available");
        }

        Customer customer = new Customer(company.get());
        customer.setDisplayName(normalizeRequired(request.displayName()));
        customer.setLegalName(trimToNull(request.legalName()));
        customer.setStatus(request.status() == null ? CustomerStatus.ACTIVE : request.status());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setPhone(trimToNull(request.phone()));
        customer.setVatNumber(vatNumber);
        customer.setPec(normalizeEmail(request.pec()));
        customer.setAddressLine1(trimToNull(request.addressLine1()));
        customer.setCity(trimToNull(request.city()));
        customer.setPostalCode(trimToNull(request.postalCode()));
        customer.setCountry(normalizeCountry(request.country()));

        return customerRepository.save(customer);
    }

    public Page<Customer> listCustomers(
            AuthPrincipal principal,
            int page,
            int size,
            String sort,
            String q
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));

        Long companyId = getRequiredCompanyId(principal);
        PageRequest pageRequest = toPageRequest(page, size, sort);
        return (q == null || q.isBlank())
                ? customerRepository.findByCompanyIdAndStatus(companyId, CustomerStatus.ACTIVE, pageRequest)
                : customerRepository.searchByCompanyIdAndStatus(companyId, CustomerStatus.ACTIVE, q.trim(), pageRequest);
    }

    public Optional<Customer> getCustomer(Long customerId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        return customerRepository.findByIdAndCompanyIdAndStatus(customerId, companyId, CustomerStatus.ACTIVE);
    }

    public Customer updateCustomer(Long customerId, UpdateCustomerRequest request, AuthPrincipal principal) {
        Long companyId = getRequiredCompanyId(principal);
        Optional<Customer> optionalCustomer = getCustomerById(principal, customerId, companyId);
        if (optionalCustomer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CustomerID " +  customerId + " does not exist");
        }
        Customer customer = optionalCustomer.get();
        if (request.displayName() != null) {
            customer.setDisplayName(normalizeRequired(request.displayName()));
        }
        if (request.legalName() != null) {
            customer.setLegalName(trimToNull(request.legalName()));
        }
        if (request.email() != null) {
            customer.setEmail(normalizeEmail(request.email()));
        }
        if (request.phone() != null) {
            customer.setPhone(trimToNull(request.phone()));
        }
        if (request.vatNumber() != null) {
            String vatNumber = normalizeVat(request.vatNumber());
            if (!vatNumber.equals(customer.getVatNumber())) {
                customerRepository.findByCompanyIdAndVatNumberAndStatus(companyId, vatNumber, CustomerStatus.ACTIVE)
                        .ifPresent(existing -> {
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vat number already used for this company");
                        });
                customer.setVatNumber(vatNumber);
            }
        }
        if (request.pec() != null) {
            customer.setPec(normalizeEmail(request.pec()));
        }
        if (request.addressLine1() != null) {
            customer.setAddressLine1(trimToNull(request.addressLine1()));
        }
        if (request.city() != null) {
            customer.setCity(trimToNull(request.city()));
        }
        if (request.postalCode() != null) {
            customer.setPostalCode(trimToNull(request.postalCode()));
        }
        if (request.country() != null) {
            customer.setCountry(normalizeCountry(request.country()));
        }

        return customerRepository.save(customer);
    }

    public Optional<Boolean> deleteCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.DELETED);
    }

    public Optional<Boolean> archiveCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.ARCHIVED);
    }

    public Optional<Boolean> restoreCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.ACTIVE);
    }

    public List<Quote> listCustomerQuotes(Long customerId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);

        Optional<Customer> optionalCustomer = customerRepository.findByIdAndCompanyIdAndStatus(
                customerId,
                companyId,
                CustomerStatus.ACTIVE
        );
        if (optionalCustomer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameters not valid");
        }

        return quoteRepository.findByCompanyIdAndCustomerIdOrderByIssueDateDesc(companyId, customerId);
    }

    private Optional<Customer> getCustomerById(AuthPrincipal principal, Long customerId, Long companyId) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));
        return customerRepository.findByIdAndCompanyIdAndStatus(customerId, companyId,CustomerStatus.ACTIVE);
    }

    private Optional<Boolean> setCustomerStatus(Long customerId, AuthPrincipal principal, CustomerStatus status) {
        Long companyId = getRequiredCompanyId(principal);
        Optional<Customer> optionalCustomer = getCustomerById(principal, customerId, companyId);
        if (optionalCustomer.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = optionalCustomer.get();
        customer.setStatus(status);
        customerRepository.save(customer);
        return Optional.of(Boolean.TRUE);
    }

    private Long getRequiredCompanyId(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        return principal.companyId();
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeVat(String vatNumber) {
        return vatNumber == null ? null : vatNumber.trim();
    }

    private String normalizeCountry(String country) {
        if (country == null || country.isBlank()) {
            return "IT";
        }
        return country.trim().toUpperCase(Locale.ROOT);
    }

    private PageRequest toPageRequest(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        Sort sortSpec = parseSort(sort);
        return PageRequest.of(safePage, safeSize, sortSpec);
    }

    private Sort parseSort(String sort) {
        String raw = (sort == null || sort.isBlank()) ? "displayName,asc" : sort.trim();
        String[] parts = raw.split(",", -1);
        String property = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "asc";

        String mappedProperty = switch (property) {
            case "legalName" -> "legalName";
            case "email" -> "email";
            case "vatNumber" -> "vatNumber";
            case "country" -> "country";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            default -> "displayName";
        };

        Sort.Direction sortDirection = "desc".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(sortDirection, mappedProperty).and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
