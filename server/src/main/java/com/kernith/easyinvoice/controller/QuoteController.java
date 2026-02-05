package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quote.CreateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quote.QuoteDetailResponse;
import com.kernith.easyinvoice.data.dto.quote.QuoteSummaryResponse;
import com.kernith.easyinvoice.data.dto.quote.UpdateQuoteRequest;
import com.kernith.easyinvoice.data.dto.invoice.InvoiceDetailResponse;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.InvoiceService;
import com.kernith.easyinvoice.service.PdfService;
import com.kernith.easyinvoice.service.QuoteService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quote endpoints for CRUD operations, status transitions, and PDF rendering.
 */
@RestController
public class QuoteController {

    private final QuoteService quoteService;
    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    public QuoteController(QuoteService quoteService, InvoiceService invoiceService, PdfService pdfService) {
        this.quoteService = quoteService;
        this.invoiceService = invoiceService;
        this.pdfService = pdfService;
    }

    /**
     * Creates a new quote.
     *
     * @param request quote creation payload
     * @param principal authenticated principal
     * @return created quote details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/quotes")
    public ResponseEntity<QuoteDetailResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteDetailResponse.from(quoteService.createQuote(request, principal)));
    }

    /**
     * Lists quotes with pagination and optional search.
     *
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @param principal authenticated principal
     * @return paged quote summaries or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/quotes")
    public ResponseEntity<Page<QuoteSummaryResponse>> listQuotes(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "issueDate,desc") String sort,
            @RequestParam(name = "q", required = false) String q,
            @CurrentUser AuthPrincipal principal
    ) {
        Page<Quote> quotes = quoteService.listQuotes(principal, page, size, sort, q);
        if (quotes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(quotes.map(QuoteSummaryResponse::from));
    }

    /**
     * Returns a single quote by id.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return quote details or {@code 400 Bad Request} if not found
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/quotes/{quoteId}")
    public ResponseEntity<QuoteDetailResponse> getQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        Optional<Quote> optionalQuote = quoteService.getQuote(quoteId, principal);
        if (optionalQuote.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(QuoteDetailResponse.from(optionalQuote.get()));
    }

    /**
     * Returns an inline PDF rendering for the quote.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return PDF bytes with inline disposition
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/quotes/{quoteId}/pdf")
    public ResponseEntity<byte[]> getQuotePdf(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        byte[] pdf = pdfService.quotePdf(quoteId, principal);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=quote-" + quoteId + ".pdf")
                .body(pdf);
    }

    /**
     * Returns a downloadable PDF rendering for the quote.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return PDF bytes with attachment disposition
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/quotes/{quoteId}/pdf-download")
    public ResponseEntity<byte[]> getQuoteDownloadPdf(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        byte[] pdf = pdfService.quotePdf(quoteId, principal);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=quote-" + quoteId + ".pdf")
                .body(pdf);
    }

    /**
     * Updates editable fields of a quote.
     *
     * @param quoteId quote identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated quote details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PatchMapping("/quotes/{quoteId}")
    public ResponseEntity<QuoteDetailResponse> updateQuote(
            @PathVariable("quoteId") Long quoteId,
            @Valid @RequestBody UpdateQuoteRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteDetailResponse.from(quoteService.updateQuote(quoteId, request, principal)));
    }

    /**
     * Archives a quote.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/quotes/{quoteId}/archive")
    public ResponseEntity<Void> archiveQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!quoteService.archiveQuote(quoteId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a quote as sent.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/quotes/{quoteId}/send")
    public ResponseEntity<Void> sendQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!quoteService.sendQuote(quoteId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a quote as accepted.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/quotes/{quoteId}/accept")
    public ResponseEntity<Void> acceptQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!quoteService.acceptQuote(quoteId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a quote as rejected.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @PostMapping("/quotes/{quoteId}/reject")
    public ResponseEntity<Void> rejectQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!quoteService.rejectQuote(quoteId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Converts a quote into an invoice.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return created invoice details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/quotes/{quoteId}/convert")
    public ResponseEntity<InvoiceDetailResponse> convertQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.createInvoiceFromQuote(quoteId, principal)));
    }
}
