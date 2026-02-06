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

/**
 * Customer use-cases including CRUD, status changes, and related quotes.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final QuoteRepository quoteRepository;

    /**
     * Creates the service with repositories.
     *
     * @param customerRepository customer repository
     * @param companyRepository company repository
     * @param quoteRepository quote repository
     */
    public CustomerService(
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            QuoteRepository quoteRepository
    ) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.quoteRepository = quoteRepository;
    }

    /**
     * Creates a new customer for the current company.
     *
     * <p>Lifecycle: validate role, ensure VAT uniqueness, load company, map fields, save.</p>
     *
     * @param request customer creation payload
     * @param principal authenticated principal
     * @return saved customer
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Customer createCustomer(CreateCustomerRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));

        Long companyId = Utils.getRequiredCompanyId(principal);
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
        customer.setDisplayName(Utils.normalizeRequired(request.displayName()));
        customer.setLegalName(Utils.trimToNull(request.legalName()));
        customer.setStatus(request.status() == null ? CustomerStatus.ACTIVE : request.status());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setPhone(Utils.trimToNull(request.phone()));
        customer.setVatNumber(vatNumber);
        customer.setPec(normalizeEmail(request.pec()));
        customer.setAddress(Utils.trimToNull(request.address()));
        customer.setCity(Utils.trimToNull(request.city()));
        customer.setPostalCode(Utils.trimToNull(request.postalCode()));
        customer.setCountry(normalizeCountry(request.country()));

        return customerRepository.save(customer);
    }

    /**
     * Lists customers with pagination, sorting, and optional search.
     *
     * @param principal authenticated principal
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @return page of customers
     * @throws ResponseStatusException if authorization fails
     */
    public Page<Customer> listCustomers(
            AuthPrincipal principal,
            int page,
            int size,
            String sort,
            String q
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));

        Long companyId = Utils.getRequiredCompanyId(principal);
        PageRequest pageRequest = toPageRequest(page, size, sort);
        return (q == null || q.isBlank())
                ? customerRepository.findByCompanyIdAndStatus(companyId, CustomerStatus.ACTIVE, pageRequest)
                : customerRepository.searchByCompanyIdAndStatus(companyId, CustomerStatus.ACTIVE, q.trim(), pageRequest);
    }

    /**
     * Retrieves a customer by id for the current company.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return optional customer
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Customer> getCustomer(Long customerId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);
        return customerRepository.findByIdAndCompanyIdAndStatus(customerId, companyId, CustomerStatus.ACTIVE);
    }

    /**
     * Updates editable fields of a customer.
     *
     * <p>Lifecycle: validate role and customer existence, enforce VAT uniqueness, save.</p>
     *
     * @param customerId customer identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated customer
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Customer updateCustomer(Long customerId, UpdateCustomerRequest request, AuthPrincipal principal) {
        Long companyId = Utils.getRequiredCompanyId(principal);
        Optional<Customer> optionalCustomer = getCustomerById(principal, customerId, companyId);
        if (optionalCustomer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CustomerID " +  customerId + " does not exist");
        }
        Customer customer = optionalCustomer.get();
        if (request.displayName() != null) {
            customer.setDisplayName(Utils.normalizeRequired(request.displayName()));
        }
        if (request.legalName() != null) {
            customer.setLegalName(Utils.trimToNull(request.legalName()));
        }
        if (request.email() != null) {
            customer.setEmail(normalizeEmail(request.email()));
        }
        if (request.phone() != null) {
            customer.setPhone(Utils.trimToNull(request.phone()));
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
        if (request.address() != null) {
            customer.setAddress(Utils.trimToNull(request.address()));
        }
        if (request.city() != null) {
            customer.setCity(Utils.trimToNull(request.city()));
        }
        if (request.postalCode() != null) {
            customer.setPostalCode(Utils.trimToNull(request.postalCode()));
        }
        if (request.country() != null) {
            customer.setCountry(normalizeCountry(request.country()));
        }

        return customerRepository.save(customer);
    }

    /**
     * Soft-deletes a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Boolean> deleteCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.DELETED);
    }

    /**
     * Archives a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Boolean> archiveCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.ARCHIVED);
    }

    /**
     * Restores an archived customer to active.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Boolean> restoreCustomer(Long customerId, AuthPrincipal principal) {
        return setCustomerStatus(customerId, principal, CustomerStatus.ACTIVE);
    }

    /**
     * Lists quotes related to a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return list of quotes
     * @throws ResponseStatusException if authorization fails or customer is missing
     */
    public List<Quote> listCustomerQuotes(Long customerId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);

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
        Long companyId = Utils.getRequiredCompanyId(principal);
        Optional<Customer> optionalCustomer = getCustomerById(principal, customerId, companyId);
        if (optionalCustomer.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = optionalCustomer.get();
        customer.setStatus(status);
        customerRepository.save(customer);
        return Optional.of(Boolean.TRUE);
    }

    // moved to Utils

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
