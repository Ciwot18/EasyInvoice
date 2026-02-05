package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.*;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoicePdfArchive;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.InvoicePdfService;
import com.kernith.easyinvoice.service.InvoiceService;
import com.kernith.easyinvoice.service.PdfService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Invoice endpoints for CRUD operations, status transitions, and PDF retrieval.
 */
@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(InvoiceService invoiceService, PdfService pdfService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.pdfService = pdfService;
        this.invoicePdfService = invoicePdfService;
    }

    /**
     * Creates a new invoice.
     *
     * @param request invoice creation payload
     * @param principal authenticated principal
     * @return created invoice details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceDetailResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.createInvoice(request, principal)));
    }

    /**
     * Lists invoices with pagination and optional search.
     *
     * @param page page index (0-based)
     * @param size page size
     * @param sort sort spec (field,dir)
     * @param q optional search query
     * @param principal authenticated principal
     * @return paged invoice summaries or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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

    /**
     * Returns a single invoice by id.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return invoice details or {@code 400 Bad Request} if not found
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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

    /**
     * Returns an inline PDF rendering for the invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return PDF bytes with inline disposition
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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

    /**
     * Returns a downloadable PDF rendering for the invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return PDF bytes with attachment disposition
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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

    /**
     * Lists stored PDF versions for an invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return list of stored PDF metadata
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/invoices/{invoiceId}/pdfs")
    public ResponseEntity<List<InvoicePdfDto>> listPdfs(
            @PathVariable Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        List<InvoicePdfDto> pdfs = invoicePdfService.listVersions(invoiceId, principal);
        return ResponseEntity.ok(pdfs);
    }

    /**
     * Downloads a previously stored invoice PDF.
     *
     * @param invoiceId invoice identifier
     * @param saveId stored PDF identifier
     * @param principal authenticated principal
     * @return PDF resource as attachment
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/invoices/{invoiceId}/pdfs/{saveId}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable Long invoiceId,
            @PathVariable Long saveId,
            @CurrentUser AuthPrincipal principal
    ) {
        InvoicePdfDownload pdf = invoicePdfService.download(invoiceId, saveId, principal);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.fileName() + "\"")
                .body(pdf.resource());
    }

    /**
     * Updates editable fields of an invoice.
     *
     * @param invoiceId invoice identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated invoice details
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PatchMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> updateInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceDetailResponse.from(invoiceService.updateInvoice(invoiceId, request, principal)));
    }

    /**
     * Issues an invoice and stores the generated PDF.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return saved PDF archive metadata
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/invoices/{invoiceId}/issue")
    public ResponseEntity<InvoicePdfArchiveSaveResponse> issueInvoice(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        InvoicePdfArchive invoicePdfArchive = invoiceService.issueInvoice(invoiceId, principal);
        return ResponseEntity.ok(InvoicePdfArchiveSaveResponse.from(invoicePdfArchive));
    }

    /**
     * Marks an invoice as paid.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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

    /**
     * Marks an invoice as overdue.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
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
