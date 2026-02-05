package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.customer.CreateCustomerRequest;
import com.kernith.easyinvoice.data.dto.customer.CustomerDetailResponse;
import com.kernith.easyinvoice.data.dto.customer.CustomerSummaryResponse;
import com.kernith.easyinvoice.data.dto.customer.UpdateCustomerRequest;
import com.kernith.easyinvoice.data.dto.quote.QuoteSummaryResponse;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer endpoints for CRUD operations and related quotes.
 */
@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer.
     *
     * @param request customer creation payload
     * @param principal authenticated principal
     * @return created customer summary
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/manager/customers")
    public ResponseEntity<CustomerSummaryResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(CustomerSummaryResponse.from(customerService.createCustomer(request, principal)));
    }

    /**
     * Returns a customer by id.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return customer details or {@code 404 Not Found} if missing
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/manager/customers/{customerId}")
    public ResponseEntity<CustomerDetailResponse> getCustomer(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        Optional<Customer> optionalCustomer = customerService.getCustomer(customerId, principal);
        if (optionalCustomer.isPresent()) {
            return ResponseEntity.ok(CustomerDetailResponse.from(optionalCustomer.get()));
        } else  {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates editable fields of a customer.
     *
     * @param customerId customer identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated customer details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PatchMapping("/manager/customers/{customerId}")
    public ResponseEntity<CustomerDetailResponse> updateCustomer(
            @PathVariable("customerId") Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(CustomerDetailResponse.from(customerService.updateCustomer(customerId, request, principal)));
    }

    /**
     * Deletes (soft-deletes) a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 404 Not Found} if missing
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @DeleteMapping("/manager/customers/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (customerService.deleteCustomer(customerId, principal).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists customers with summary payloads when {@code type=summary}.
     *
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @param principal authenticated principal
     * @return paged customer summaries or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping(value = "/manager/customers", params = "type=summary")
    public ResponseEntity<Page<CustomerSummaryResponse>> listCustomersSummary(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "displayName,asc") String sort,
            @RequestParam(name = "q", required = false) String q,
            @CurrentUser AuthPrincipal principal
    ) {
        Page<Customer> customers = customerService.listCustomers(principal, page, size, sort, q);
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers.map(CustomerSummaryResponse::from));
    }

    /**
     * Lists customers with detail payloads.
     *
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @param principal authenticated principal
     * @return paged customer details or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/manager/customers")
    public ResponseEntity<Page<CustomerDetailResponse>> listCustomers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "displayName,asc") String sort,
            @RequestParam(name = "q", required = false) String q,
            @CurrentUser AuthPrincipal principal
    ) {
        Page<Customer> customers = customerService.listCustomers(principal, page, size, sort, q);
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers.map(CustomerDetailResponse::from));
    }

    /**
     * Archives a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 404 Not Found} if missing
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/manager/customers/{customerId}/archive")
    public ResponseEntity<Void> archiveCustomer(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (customerService.archiveCustomer(customerId, principal).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores an archived customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 404 Not Found} if missing
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/manager/customers/{customerId}/restore")
    public ResponseEntity<Void> restoreCustomer(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (customerService.restoreCustomer(customerId, principal).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists quotes related to a customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return list of quotes or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/customer/{customerId}/quotes")
    public ResponseEntity<List<QuoteSummaryResponse>> listCustomerQuotes(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        List<Quote> quotes = customerService.listCustomerQuotes(customerId, principal);
        if (quotes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(quotes.stream().map(QuoteSummaryResponse::from).toList());
    }
}
