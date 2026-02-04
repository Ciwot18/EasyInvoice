package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.UpdateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.DiscountType;
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

@Service
public class InvoiceItemService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public InvoiceItemService(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    public InvoiceItem addInvoiceItem(Long invoiceId, CreateInvoiceItemRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getEditableInvoice(invoiceId, principal);

        InvoiceItem item = new InvoiceItem(invoice);
        item.setPosition(request.position());
        item.setDescription(normalizeRequired(request.description()));
        item.setNotes(trimToNull(request.notes()));
        item.setQuantity(defaultBigDecimal(request.quantity(), BigDecimal.ONE));
        item.setUnit(trimToNull(request.unit()));
        item.setUnitPrice(defaultBigDecimal(request.unitPrice(), BigDecimal.ZERO));
        item.setTaxRate(defaultBigDecimal(request.taxRate(), BigDecimal.ZERO));
        item.setDiscountType(mapDiscountType(request.discountType()));
        item.setDiscountValue(defaultBigDecimal(request.discountValue(), BigDecimal.ZERO));

        InvoiceItem saved = invoiceItemRepository.save(item);
        recalcInvoiceTotals(invoice);
        return saved;
    }

    public List<InvoiceItem> listInvoiceItems(Long invoiceId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Invoice invoice = getInvoiceOrThrow(invoiceId, principal);
        return invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId());
    }

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
            item.setDescription(normalizeRequired(request.description()));
        }
        if (request.notes() != null) {
            item.setNotes(trimToNull(request.notes()));
        }
        if (request.quantity() != null) {
            item.setQuantity(request.quantity());
        }
        if (request.unit() != null) {
            item.setUnit(trimToNull(request.unit()));
        }
        if (request.unitPrice() != null) {
            item.setUnitPrice(request.unitPrice());
        }
        if (request.taxRate() != null) {
            item.setTaxRate(request.taxRate());
        }
        if (request.discountType() != null) {
            item.setDiscountType(mapDiscountType(request.discountType()));
        }
        if (request.discountValue() != null) {
            item.setDiscountValue(request.discountValue());
        }

        InvoiceItem saved = invoiceItemRepository.save(item);
        recalcInvoiceTotals(invoice);
        return saved;
    }

    public Optional<Boolean> deleteInvoiceItem(Long invoiceId, Long itemId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Optional<Invoice> optionalInvoice = invoiceRepository.findByIdAndCompanyId(
                invoiceId,
                getRequiredCompanyId(principal)
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
        Long companyId = getRequiredCompanyId(principal);
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

    private BigDecimal defaultBigDecimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeRequired(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long getRequiredCompanyId(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        return principal.companyId();
    }

    private DiscountType mapDiscountType(DiscountType discountType) {
        if (discountType == null) {
            return DiscountType.NONE;
        }
        return DiscountType.valueOf(discountType.name());
    }
}
