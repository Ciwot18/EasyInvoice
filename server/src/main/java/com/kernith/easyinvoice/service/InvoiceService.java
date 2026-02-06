package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.CreateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoice.UpdateInvoiceRequest;
import com.kernith.easyinvoice.data.model.*;
import com.kernith.easyinvoice.data.repository.*;
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

/**
 * Invoice use-cases including creation, updates, listing, and status transitions.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final InvoicePdfService invoicePdfService;

    /**
     * Creates the service with repositories and supporting services.
     *
     * @param invoiceRepository invoice repository
     * @param invoiceItemRepository invoice item repository
     * @param companyRepository company repository
     * @param customerRepository customer repository
     * @param quoteRepository quote repository
     * @param invoicePdfService PDF archive service
     */
    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            CompanyRepository companyRepository,
            CustomerRepository customerRepository,
            QuoteRepository quoteRepository,
            InvoicePdfService invoicePdfService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.quoteRepository = quoteRepository;
        this.invoicePdfService = invoicePdfService;
    }

    /**
     * Creates a new invoice from scratch and computes totals from items.
     *
     * <p>Lifecycle: validate role and input, allocate invoice number, persist invoice,
     * create items, recalc totals, then save updated invoice.</p>
     *
     * @param request invoice creation payload
     * @param principal authenticated principal
     * @return saved invoice
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Invoice createInvoice(CreateInvoiceRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));

        Long companyId = Utils.getRequiredCompanyId(principal);
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
        invoice.setTitle(Utils.trimToNull(request.title()));
        invoice.setNotes(Utils.trimToNull(request.notes()));
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(request.dueDate());
        invoice.setCurrency(Utils.normalizeCurrency(request.currency()));

        Invoice savedInvoice = invoiceRepository.save(invoice);
        List<InvoiceItem> items = new ArrayList<>();
        request.items().forEach(itemRequest -> {
            InvoiceItem item = new InvoiceItem(
                    savedInvoice,
                    itemRequest.position(),
                    Utils.normalizeRequired(itemRequest.description()),
                    Utils.defaultBigDecimal(itemRequest.quantity(), BigDecimal.ONE),
                    Utils.defaultBigDecimal(itemRequest.unitPrice(), BigDecimal.ZERO),
                    Utils.defaultBigDecimal(itemRequest.taxRate(), BigDecimal.ZERO),
                    Utils.mapDiscountType(itemRequest.discountType()),
                    Utils.defaultBigDecimal(itemRequest.discountValue(), BigDecimal.ZERO)
            );
            item.setNotes(Utils.trimToNull(itemRequest.notes()));
            item.setUnit(Utils.trimToNull(itemRequest.unit()));
            items.add(item);
        });

        invoiceItemRepository.saveAll(items);
        savedInvoice.recalculateTotalsFromItems(items);
        return invoiceRepository.save(savedInvoice);
    }

    /**
     * Creates a new invoice by converting a quote.
     *
     * <p>Lifecycle: validate role and quote, allocate invoice number, copy items,
     * save invoice, and recalc totals if items are present.</p>
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return saved invoice
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Invoice createInvoiceFromQuote(Long quoteId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);

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

    /**
     * Retrieves an invoice by id for the current company.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return optional invoice
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Invoice> getInvoice(Long invoiceId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);
        return invoiceRepository.findByIdAndCompanyId(invoiceId, companyId);
    }

    /**
     * Updates editable fields on a draft invoice.
     *
     * <p>Lifecycle: validate role and draft status, apply updates, then save.</p>
     *
     * @param invoiceId invoice identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated invoice
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Invoice updateInvoice(Long invoiceId, UpdateInvoiceRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getEditableInvoice(invoiceId, principal);

        if (request.title() != null) {
            invoice.setTitle(Utils.trimToNull(request.title()));
        }
        if (request.notes() != null) {
            invoice.setNotes(Utils.trimToNull(request.notes()));
        }
        if (request.issueDate() != null) {
            invoice.setIssueDate(request.issueDate());
        }
        if (request.dueDate() != null) {
            invoice.setDueDate(request.dueDate());
        }
        if (request.currency() != null) {
            invoice.setCurrency(Utils.normalizeCurrency(request.currency()));
        }

        return invoiceRepository.save(invoice);
    }

    /**
     * Lists invoices with pagination, sorting, and optional search.
     *
     * @param principal authenticated principal
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @return page of invoices
     * @throws ResponseStatusException if authorization fails
     */
    public Page<Invoice> listInvoices(AuthPrincipal principal, int page, int size, String sort, String q) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);
        PageRequest pageRequest = toPageRequest(page, size, sort);
        if (q == null || q.isBlank()) {
            return invoiceRepository.findByCompanyId(companyId, pageRequest);
        }
        return invoiceRepository.searchByCompanyId(companyId, q.trim(), pageRequest);
    }

    /**
     * Issues an invoice and stores its PDF snapshot.
     *
     * <p>Lifecycle: transition status to ISSUED, then generate and archive PDF.</p>
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return saved PDF archive
     * @throws ResponseStatusException if authorization fails
     */
    public InvoicePdfArchive issueInvoice(Long invoiceId, AuthPrincipal principal) {
        Boolean result = transitionStatus(invoiceId, principal, InvoiceStatus.ISSUED);
        return invoicePdfService.saveIssuedPdf(invoiceId, principal);
    }

    /**
     * Marks an invoice as paid.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return {@code true} if the invoice was found and updated
     * @throws ResponseStatusException if authorization fails
     */
    public Boolean payInvoice(Long invoiceId, AuthPrincipal principal) {
        return transitionStatus(invoiceId, principal, InvoiceStatus.PAID);
    }

    /**
     * Marks an invoice as overdue.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return {@code true} if the invoice was found and updated
     * @throws ResponseStatusException if authorization fails
     */
    public Boolean markInvoiceOverdue(Long invoiceId, AuthPrincipal principal) {
        return transitionStatus(invoiceId, principal, InvoiceStatus.OVERDUE);
    }

    private Boolean transitionStatus(
            Long invoiceId,
            AuthPrincipal principal,
            InvoiceStatus newStatus
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);
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
        Long companyId = Utils.getRequiredCompanyId(principal);
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

}
