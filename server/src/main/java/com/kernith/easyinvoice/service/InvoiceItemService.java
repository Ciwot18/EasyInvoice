package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.UpdateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.helper.Utils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manages invoice items and keeps invoice totals in sync.
 */
@Service
public class InvoiceItemService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    /**
     * Creates the service with repositories.
     *
     * @param invoiceRepository invoice repository
     * @param invoiceItemRepository invoice item repository
     */
    public InvoiceItemService(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    /**
     * Adds a new item to an invoice and recalculates totals.
     *
     * <p>Lifecycle: validate role and invoice state, create item, save, then recalc totals.</p>
     *
     * @param invoiceId invoice identifier
     * @param request item creation payload
     * @param principal authenticated principal
     * @return saved invoice item
     * @throws ResponseStatusException if role or invoice state is invalid
     */
    public InvoiceItem addInvoiceItem(Long invoiceId, CreateInvoiceItemRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getEditableInvoice(invoiceId, principal);

        InvoiceItem item = new InvoiceItem(invoice);
        item.setPosition(request.position());
        item.setDescription(Utils.normalizeRequired(request.description()));
        item.setNotes(Utils.trimToNull(request.notes()));
        item.setQuantity(Utils.defaultBigDecimal(request.quantity(), BigDecimal.ONE));
        item.setUnit(Utils.trimToNull(request.unit()));
        item.setUnitPrice(Utils.defaultBigDecimal(request.unitPrice(), BigDecimal.ZERO));
        item.setTaxRate(Utils.defaultBigDecimal(request.taxRate(), BigDecimal.ZERO));
        item.setDiscountType(Utils.mapDiscountType(request.discountType()));
        item.setDiscountValue(Utils.defaultBigDecimal(request.discountValue(), BigDecimal.ZERO));

        InvoiceItem saved = invoiceItemRepository.save(item);
        recalcInvoiceTotals(invoice);
        return saved;
    }

    /**
     * Lists items for a given invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return list of invoice items
     * @throws ResponseStatusException if role or invoice state is invalid
     */
    public List<InvoiceItem> listInvoiceItems(Long invoiceId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getInvoiceOrThrow(invoiceId, principal);
        return invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId());
    }

    /**
     * Updates an existing invoice item and recalculates totals.
     *
     * <p>Lifecycle: validate role and invoice state, update fields, save, then recalc totals.</p>
     *
     * @param invoiceId invoice identifier
     * @param itemId item identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return saved invoice item
     * @throws ResponseStatusException if role or invoice state is invalid
     */
    public InvoiceItem updateInvoiceItem(
            Long invoiceId,
            Long itemId,
            UpdateInvoiceItemRequest request,
            AuthPrincipal principal
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getEditableInvoice(invoiceId, principal);

        Optional<InvoiceItem> optionalItem = invoiceItemRepository.findByIdAndInvoiceId(itemId, invoice.getId());
        if (optionalItem.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameters not valid");
        }
        InvoiceItem item = optionalItem.get();

        if (request.position() != null) {
            item.setPosition(request.position());
        }
        if (request.description() != null) {
            item.setDescription(Utils.normalizeRequired(request.description()));
        }
        if (request.notes() != null) {
            item.setNotes(Utils.trimToNull(request.notes()));
        }
        if (request.quantity() != null) {
            item.setQuantity(request.quantity());
        }
        if (request.unit() != null) {
            item.setUnit(Utils.trimToNull(request.unit()));
        }
        if (request.unitPrice() != null) {
            item.setUnitPrice(request.unitPrice());
        }
        if (request.taxRate() != null) {
            item.setTaxRate(request.taxRate());
        }
        if (request.discountType() != null) {
            item.setDiscountType(Utils.mapDiscountType(request.discountType()));
        }
        if (request.discountValue() != null) {
            item.setDiscountValue(request.discountValue());
        }

        InvoiceItem saved = invoiceItemRepository.save(item);
        recalcInvoiceTotals(invoice);
        return saved;
    }

    /**
     * Deletes an invoice item and recalculates totals if found.
     *
     * @param invoiceId invoice identifier
     * @param itemId item identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if role or invoice state is invalid
     */
    public Optional<Boolean> deleteInvoiceItem(Long invoiceId, Long itemId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Optional<Invoice> optionalInvoice = invoiceRepository.findByIdAndCompanyId(
                invoiceId,
                Utils.getRequiredCompanyId(principal)
        );
        if (optionalInvoice.isEmpty()) {
            return Optional.empty();
        }
        Invoice invoice = optionalInvoice.get();
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is not editable");
        }

        Optional<InvoiceItem> optionalItem = invoiceItemRepository.findByIdAndInvoiceId(itemId, invoice.getId());
        if (optionalItem.isEmpty()) {
            return Optional.empty();
        }
        invoiceItemRepository.delete(optionalItem.get());
        recalcInvoiceTotals(invoice);
        return Optional.of(Boolean.TRUE);
    }

    private Invoice getInvoiceOrThrow(Long invoiceId, AuthPrincipal principal) {
        Long companyId = Utils.getRequiredCompanyId(principal);
        Optional<Invoice> optionalInvoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId);
        if (optionalInvoice.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is not editable");
        }
        return optionalInvoice.get();
    }

    private Invoice getEditableInvoice(Long invoiceId, AuthPrincipal principal) {
        Invoice invoice = getInvoiceOrThrow(invoiceId, principal);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is not editable");
        }
        return invoice;
    }

    private void recalcInvoiceTotals(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId());
        invoice.recalculateTotalsFromItems(items);
        invoiceRepository.save(invoice);
    }

}
