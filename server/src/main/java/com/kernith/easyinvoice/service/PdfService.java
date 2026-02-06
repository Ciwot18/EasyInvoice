package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.*;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import com.kernith.easyinvoice.helper.HtmlToPdfRenderer;
import com.kernith.easyinvoice.helper.PdfHtmlBuilder;
import com.kernith.easyinvoice.helper.adapter.InvoicePdfAdapter;
import com.kernith.easyinvoice.helper.adapter.PdfDocumentView;
import com.kernith.easyinvoice.helper.adapter.QuotePdfAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Generates PDF documents for invoices and quotes.
 */
@Service
public class PdfService {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository invoiceItemRepo;
    private final QuoteRepository quoteRepo;
    private final QuoteItemRepository quoteItemRepo;

    /**
     * Creates the service with repositories needed for rendering.
     *
     * @param invoiceRepo invoice repository
     * @param invoiceItemRepo invoice item repository
     * @param quoteRepo quote repository
     * @param quoteItemRepo quote item repository
     */
    public PdfService(
            InvoiceRepository invoiceRepo,
            InvoiceItemRepository invoiceItemRepo,
            QuoteRepository quoteRepo,
            QuoteItemRepository quoteItemRepo
    ) {
        this.invoiceRepo = invoiceRepo;
        this.invoiceItemRepo = invoiceItemRepo;
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
    }

    /**
     * Builds the PDF for an invoice.
     *
     * <p>Lifecycle: load invoice and items, adapt to PDF view, build HTML, render PDF.</p>
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return PDF bytes
     * @throws org.springframework.web.server.ResponseStatusException if the invoice does not belong to the company
     */
    @Transactional(readOnly = true)
    public byte[] invoicePdf(Long invoiceId, AuthPrincipal principal) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!(principal.companyId() == inv.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }
        List<InvoiceItem> items = invoiceItemRepo.findByInvoiceIdOrderByPositionAsc(invoiceId);
        Company company = inv.getCompany();
        Customer customer = inv.getCustomer();

        PdfDocumentView view = new InvoicePdfAdapter(inv, items, company, customer);

        String html = new PdfHtmlBuilder()
                .document(view)
                .companyAddress(company.getAddress())
                .customerAddress(customer.getAddress())
                .build();

        return HtmlToPdfRenderer.render(html);
    }

    /**
     * Builds the PDF for a quote.
     *
     * <p>Lifecycle: load quote and items, adapt to PDF view, build HTML, render PDF.</p>
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return PDF bytes
     * @throws org.springframework.web.server.ResponseStatusException if the quote does not belong to the company
     */
    @Transactional(readOnly = true)
    public byte[] quotePdf(Long quoteId, AuthPrincipal principal) {
        Quote quote = quoteRepo.findById(quoteId).orElseThrow();
        if (!(principal.companyId() == quote.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }
        List<QuoteItem> items = quoteItemRepo.findByQuoteIdOrderByPositionAsc(quoteId);
        Company company = quote.getCompany();
        Customer customer = quote.getCustomer();

        PdfDocumentView view = new QuotePdfAdapter(quote, items, company, customer);

        String html = new PdfHtmlBuilder()
                .document(view)
                .companyAddress(company.getAddress())
                .customerAddress(customer.getAddress())
                .build();

        return HtmlToPdfRenderer.render(html);
    }
}
