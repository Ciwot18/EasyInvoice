package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.CreateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoice.InvoiceDetailResponse;
import com.kernith.easyinvoice.data.dto.invoice.InvoiceSummaryResponse;
import com.kernith.easyinvoice.data.dto.invoice.UpdateInvoiceRequest;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.InvoiceService;
import com.kernith.easyinvoice.service.PdfService;
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
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    public InvoiceController(InvoiceService invoiceService, PdfService pdfService) {
        this.invoiceService = invoiceService;
        this.pdfService = pdfService;
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceDetailResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.createInvoice(request, principal)));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceSummaryResponse>> listInvoices(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "issueDate,desc") String sort,
            @RequestParam(name = "q", required = false) String q,
            @CurrentUser AuthPrincipal principal
    ) {
        Page<Invoice> invoices = invoiceService.listInvoices(principal, page, size, sort, q);
        if (invoices.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(invoices.map(InvoiceSummaryResponse::from));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> getInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        Optional<Invoice> optionalInvoice = invoiceService.getInvoice(invoiceId, principal);
        if (optionalInvoice.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(InvoiceDetailResponse.from(optionalInvoice.get()));
    }

    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> getinvoicePdf(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        byte[] pdf = pdfService.invoicePdf(invoiceId, principal);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=invoice-" + invoiceId + ".pdf")
                .body(pdf);
    }

    @GetMapping("/invoices/{invoiceId}/pdf-download")
    public ResponseEntity<byte[]> getinvoiceDownloadPdf(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        byte[] pdf = pdfService.invoicePdf(invoiceId, principal);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice-" + invoiceId + ".pdf")
                .body(pdf);
    }

    @PatchMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> updateInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.updateInvoice(invoiceId, request, principal)));
    }

    @PostMapping("/invoices/{invoiceId}/issue")
    public ResponseEntity<Void> issueInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!invoiceService.issueInvoice(invoiceId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    public ResponseEntity<Void> payInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!invoiceService.payInvoice(invoiceId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invoices/{invoiceId}/overdue")
    public ResponseEntity<Void> markInvoiceOverdue(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (!invoiceService.markInvoiceOverdue(invoiceId, principal)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }
}