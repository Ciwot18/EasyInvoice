package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quote.CreateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quote.UpdateQuoteRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import com.kernith.easyinvoice.helper.Utils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;

    public QuoteService(
            QuoteRepository quoteRepository,
            QuoteItemRepository quoteItemRepository,
            CompanyRepository companyRepository,
            CustomerRepository customerRepository
    ) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
    }

    public Quote createQuote(CreateQuoteRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));

        Long companyId = getRequiredCompanyId(principal);
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if  (optionalCompany.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found");
        }

        Optional<Customer> optionalCustomer = customerRepository.findByIdAndCompanyIdAndStatus(request.customerId(), companyId, CustomerStatus.ACTIVE);
        if  (optionalCustomer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer specified is not available");
        }
        // Real logic starts
        Company company = optionalCompany.get();
        Customer customer = optionalCustomer.get();
        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();  // Make sure that the date is compiled
        int quoteYear = issueDate.getYear();
        Integer maxNumber = quoteRepository.findMaxQuoteNumber(companyId, quoteYear);
        int quoteNumber = (maxNumber == null ? 0 : maxNumber) + 1;

        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote items are required");
        }

        Quote quote = new Quote(company, customer);
        quote.setQuoteYear(quoteYear);
        quote.setQuoteNumber(quoteNumber);
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setTitle(trimToNull(request.title()));
        quote.setNotes(trimToNull(request.notes()));
        quote.setIssueDate(issueDate);
        quote.setValidUntil(request.validUntil());
        quote.setCurrency(normalizeCurrency(request.currency()));
        // At the moment I am just creating the Quote to then be able of adding the elements
        // The calculations will be made at the end of the insert of the items
        Quote savedQuote = quoteRepository.save(quote);
        List<QuoteItem> items = new ArrayList<>();
        // First I add all the items
        request.items().forEach(itemRequest -> {
            QuoteItem item = new QuoteItem(
                    savedQuote,
                    itemRequest.position(),
                    normalizeRequired(itemRequest.description()),
                    defaultBigDecimal(itemRequest.quantity(), BigDecimal.ONE),
                    defaultBigDecimal(itemRequest.unitPrice(), BigDecimal.ZERO),
                    defaultBigDecimal(itemRequest.taxRate(), BigDecimal.ZERO),
                    itemRequest.discountType() == null ? DiscountType.NONE : itemRequest.discountType(),
                    defaultBigDecimal(itemRequest.discountValue(), BigDecimal.ZERO)
            );
            item.setNotes(trimToNull(itemRequest.notes()));
            item.setUnit(trimToNull(itemRequest.unit()));
            items.add(item);
        });

        // Then save them all
        quoteItemRepository.saveAll(items);
        // Finally recalculate the totals and save the original Quote
        savedQuote.recalculateTotalsFromItems(items);
        return quoteRepository.save(savedQuote);
    }

    public Optional<Quote> getQuote(Long quoteId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        return quoteRepository.findByIdAndCompanyId(quoteId, companyId);
    }

    // Update just in the description, to modify one of the items there is a specific endpoint
    public Quote updateQuote(Long quoteId, UpdateQuoteRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(quoteId, companyId);
        if  (optionalQuote.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }
        Quote quote = optionalQuote.get();
        if (request.title() != null) {
            quote.setTitle(trimToNull(request.title()));
        }
        if (request.notes() != null) {
            quote.setNotes(trimToNull(request.notes()));
        }
        if (request.issueDate() != null) {
            quote.setIssueDate(request.issueDate());
        }
        if (request.validUntil() != null) {
            quote.setValidUntil(request.validUntil());
        }
        if (request.currency() != null) {
            quote.setCurrency(normalizeCurrency(request.currency()));
        }

        return quoteRepository.save(quote);
    }

    public Page<Quote> listQuotes(AuthPrincipal principal, int page, int size, String sort, String q) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        PageRequest pageRequest = toPageRequest(page, size, sort);
        if (q == null || q.isBlank()) {
            return quoteRepository.findByCompanyId(companyId, pageRequest);
        }
        return quoteRepository.searchByCompanyId(companyId, q.trim(), pageRequest);
    }

    public Boolean archiveQuote(Long quoteId, AuthPrincipal principal) {
        return transitionStatus(quoteId, principal, null, QuoteStatus.ARCHIVED);
    }

    public Boolean sendQuote(Long quoteId, AuthPrincipal principal) {
        return transitionStatus(quoteId, principal, QuoteStatus.DRAFT, QuoteStatus.SENT);
    }

    public Boolean acceptQuote(Long quoteId, AuthPrincipal principal) {
        return transitionStatus(quoteId, principal, QuoteStatus.SENT, QuoteStatus.ACCEPTED);
    }

    public Boolean rejectQuote(Long quoteId, AuthPrincipal principal) {
        return transitionStatus(quoteId, principal, QuoteStatus.SENT, QuoteStatus.REJECTED);
    }

    public Boolean convertQuote(Long quoteId, AuthPrincipal principal) {
        return transitionStatus(quoteId, principal, QuoteStatus.ACCEPTED, QuoteStatus.CONVERTED);
    }

    // TODO: Change this with the Design Pattern STATE
    private Boolean transitionStatus(
            Long quoteId,
            AuthPrincipal principal,
            QuoteStatus expectedStatus,
            QuoteStatus newStatus
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));
        Long companyId = getRequiredCompanyId(principal);
        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(quoteId, companyId);
        if (optionalQuote.isEmpty()) {
            return Boolean.FALSE;
        }
        Quote quote = optionalQuote.get();
        if (expectedStatus != null && quote.getStatus() != expectedStatus) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition from " + quote.getStatus() + " to " + newStatus
            );
        }
        quote.setStatus(newStatus);
        quoteRepository.save(quote);
        return Boolean.TRUE;
    }

    private Long getRequiredCompanyId(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        return principal.companyId();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal defaultBigDecimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "EUR";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private PageRequest toPageRequest(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        Sort sortSpec = parseSort(sort);
        return PageRequest.of(safePage, safeSize, sortSpec);
    }

    private Sort parseSort(String sort) {
        String raw = (sort == null || sort.isBlank()) ? "issueDate,desc" : sort.trim();
        String[] parts = raw.split(",", -1);
        String property = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "desc";

        String mappedProperty = switch (property) {
            case "status" -> "status";
            case "quoteNumber" -> "quoteNumber";
            case "quoteYear" -> "quoteYear";
            case "title" -> "title";
            case "totalAmount" -> "totalAmount";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "customerDisplayName" -> "customer.displayName";
            default -> "issueDate";
        };

        Sort.Direction sortDirection = "asc".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, mappedProperty).and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
