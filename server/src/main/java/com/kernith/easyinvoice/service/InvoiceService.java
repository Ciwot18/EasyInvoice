package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.CreateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoice.UpdateInvoiceRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            CompanyRepository companyRepository,
            CustomerRepository customerRepository,
            QuoteRepository quoteRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.quoteRepository = quoteRepository;
    }

    public Invoice createInvoice(CreateInvoiceRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));

        Long companyId = getRequiredCompanyId(principal);
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found");
        }

        Optional<Customer> optionalCustomer = customerRepository.findByIdAndCompanyIdAndStatus(
                request.customerId(),
                companyId,
                CustomerStatus.ACTIVE
        );
        if (optionalCustomer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer specified is not available");
        }

        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();
        int invoiceYear = issueDate.getYear();
        Integer maxNumber = invoiceRepository.findMaxInvoiceNumber(companyId, invoiceYear);
        int invoiceNumber = (maxNumber == null ? 0 : maxNumber) + 1;

        // On creation from "empty" items must be specified
        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice items are required");
        }

        Invoice invoice = new Invoice(optionalCompany.get(), optionalCustomer.get());
        invoice.setInvoiceYear(invoiceYear);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTitle(trimToNull(request.title()));
        invoice.setNotes(trimToNull(request.notes()));
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(request.dueDate());
        invoice.setCurrency(normalizeCurrency(request.currency()));

        Invoice savedInvoice = invoiceRepository.save(invoice);
        List<InvoiceItem> items = new ArrayList<>();
        request.items().forEach(itemRequest -> {
            InvoiceItem item = new InvoiceItem(
                    savedInvoice,
                    itemRequest.position(),
                    normalizeRequired(itemRequest.description()),
                    defaultBigDecimal(itemRequest.quantity(), BigDecimal.ONE),
                    defaultBigDecimal(itemRequest.unitPrice(), BigDecimal.ZERO),
                    defaultBigDecimal(itemRequest.taxRate(), BigDecimal.ZERO),
                    mapDiscountType(itemRequest.discountType()),
                    defaultBigDecimal(itemRequest.discountValue(), BigDecimal.ZERO)
            );
            item.setNotes(trimToNull(itemRequest.notes()));
            item.setUnit(trimToNull(itemRequest.unit()));
            items.add(item);
        });

        invoiceItemRepository.saveAll(items);
        savedInvoice.recalculateTotalsFromItems(items);
        return invoiceRepository.save(savedInvoice);
    }

    public Invoice createInvoiceFromQuote(Long quoteId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);

        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(quoteId, companyId);
        if (optionalQuote.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source quote not found");
        }
        Quote quote = optionalQuote.get();
        LocalDate issueDate = LocalDate.now();
        int invoiceYear = issueDate.getYear();
        Integer maxNumber = invoiceRepository.findMaxInvoiceNumber(companyId, invoiceYear);
        int invoiceNumber = (maxNumber == null ? 0 : maxNumber) + 1;

        Invoice invoice = new Invoice(quote);
        invoice.setInvoiceYear(invoiceYear);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(null);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        List<InvoiceItem> items = savedInvoice.getItems();
        if (items != null && !items.isEmpty()) {
            invoiceItemRepository.saveAll(items);
            savedInvoice.recalculateTotalsFromItems(items);
            savedInvoice = invoiceRepository.save(savedInvoice);
        }
        return savedInvoice;
    }

    public Optional<Invoice> getInvoice(Long invoiceId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        return invoiceRepository.findByIdAndCompanyId(invoiceId, companyId);
    }

    public Invoice updateInvoice(Long invoiceId, UpdateInvoiceRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getEditableInvoice(invoiceId, principal);

        if (request.title() != null) {
            invoice.setTitle(trimToNull(request.title()));
        }
        if (request.notes() != null) {
            invoice.setNotes(trimToNull(request.notes()));
        }
        if (request.issueDate() != null) {
            invoice.setIssueDate(request.issueDate());
        }
        if (request.dueDate() != null) {
            invoice.setDueDate(request.dueDate());
        }
        if (request.currency() != null) {
            invoice.setCurrency(normalizeCurrency(request.currency()));
        }

        return invoiceRepository.save(invoice);
    }

    public Page<Invoice> listInvoices(AuthPrincipal principal, int page, int size, String sort, String q) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        PageRequest pageRequest = toPageRequest(page, size, sort);
        if (q == null || q.isBlank()) {
            return invoiceRepository.findByCompanyId(companyId, pageRequest);
        }
        return invoiceRepository.searchByCompanyId(companyId, q.trim(), pageRequest);
    }

    public Boolean issueInvoice(Long invoiceId, AuthPrincipal principal) {
        return transitionStatus(invoiceId, principal, InvoiceStatus.ISSUED);
    }

    public Boolean payInvoice(Long invoiceId, AuthPrincipal principal) {
        return transitionStatus(invoiceId, principal, InvoiceStatus.PAID);
    }

    public Boolean markInvoiceOverdue(Long invoiceId, AuthPrincipal principal) {
        return transitionStatus(invoiceId, principal, InvoiceStatus.OVERDUE);
    }

    private Boolean transitionStatus(
            Long invoiceId,
            AuthPrincipal principal,
            InvoiceStatus newStatus
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = getRequiredCompanyId(principal);
        Optional<Invoice> optionalInvoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId);
        if (optionalInvoice.isEmpty()) {
            return Boolean.FALSE;
        }
        Invoice invoice = optionalInvoice.get();
        switch (newStatus) {
            case DRAFT -> invoice.draft();
            case ISSUED -> invoice.issue();
            case PAID -> invoice.pay();
            case OVERDUE -> invoice.overdue();
            case ARCHIVED -> invoice.archive();
        }
        invoiceRepository.save(invoice);
        return Boolean.TRUE;
    }

    private Invoice getEditableInvoice(Long invoiceId, AuthPrincipal principal) {
        Long companyId = getRequiredCompanyId(principal);
        Optional<Invoice> optionalInvoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId);
        if (optionalInvoice.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }
        Invoice invoice = optionalInvoice.get();
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is not editable");
        }
        return invoice;
    }

    private Long getRequiredCompanyId(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        return principal.companyId();
    }

    private BigDecimal defaultBigDecimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
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
            case "invoiceNumber" -> "invoiceNumber";
            case "invoiceYear" -> "invoiceYear";
            case "title" -> "title";
            case "totalAmount" -> "totalAmount";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "dueDate" -> "dueDate";
            case "customerDisplayName" -> "customer.displayName";
            default -> "issueDate";
        };

        Sort.Direction sortDirection = "asc".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, mappedProperty).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private DiscountType mapDiscountType(DiscountType discountType) {
        if (discountType == null) {
            return DiscountType.NONE;
        }
        return DiscountType.valueOf(discountType.name());
    }
}
