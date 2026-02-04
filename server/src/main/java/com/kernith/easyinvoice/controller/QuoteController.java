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

@RestController
public class QuoteController {

    private final QuoteService quoteService;
    private final InvoiceService invoiceService;

    public QuoteController(QuoteService quoteService, InvoiceService invoiceService) {
        this.quoteService = quoteService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/quotes")
    public ResponseEntity<QuoteDetailResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteDetailResponse.from(quoteService.createQuote(request, principal)));
    }

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

    @PatchMapping("/quotes/{quoteId}")
    public ResponseEntity<QuoteDetailResponse> updateQuote(
            @PathVariable("quoteId") Long quoteId,
            @Valid @RequestBody UpdateQuoteRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteDetailResponse.from(quoteService.updateQuote(quoteId, request, principal)));
    }

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

    @PostMapping("/quotes/{quoteId}/convert")
    public ResponseEntity<InvoiceDetailResponse> convertQuote(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.createInvoiceFromQuote(quoteId, principal)));
    }
}
