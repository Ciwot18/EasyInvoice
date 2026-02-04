package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.UpdateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.repository.InvoiceItemRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceItemServiceTests {

    @Test
    void addInvoiceItemCreatesAndRecalculatesTotals() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.save(any(InvoiceItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId()))
                .thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateInvoiceItemRequest req = new CreateInvoiceItemRequest(
                1,
                " Item ",
                " Notes ",
                BigDecimal.ONE,
                "hrs",
                BigDecimal.TEN,
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        InvoiceItem saved = service.addInvoiceItem(77L, req, principal);

        assertEquals("Item", saved.getDescription());
        assertEquals("Notes", saved.getNotes());
        verify(invoiceItemRepository).save(any(InvoiceItem.class));
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void addInvoiceItemThrowsWhenNotEditable() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));

        CreateInvoiceItemRequest req = new CreateInvoiceItemRequest(
                1,
                "Item",
                null,
                BigDecimal.ONE,
                null,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertThrows(ResponseStatusException.class, () -> service.addInvoiceItem(77L, req, principal));
    }

    @Test
    void updateInvoiceItemThrowsWhenMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByIdAndInvoiceId(55L, invoice.getId())).thenReturn(Optional.empty());

        UpdateInvoiceItemRequest req = new UpdateInvoiceItemRequest(2, "Updated", null, null, null, null, null, null, null);
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> service.updateInvoiceItem(77L, 55L, req, principal));
    }

    @Test
    void updateInvoiceItemUpdatesFields() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        InvoiceItem item = new InvoiceItem(invoice);
        item.setDescription("Old");
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByIdAndInvoiceId(55L, invoice.getId())).thenReturn(Optional.of(item));
        when(invoiceItemRepository.save(any(InvoiceItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId()))
                .thenReturn(List.of(item));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateInvoiceItemRequest req = new UpdateInvoiceItemRequest(2, " Updated ", null, null, null, null, null, null, null);
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        InvoiceItem updated = service.updateInvoiceItem(77L, 55L, req, principal);
        assertEquals("Updated", updated.getDescription());
        verify(invoiceItemRepository).save(any(InvoiceItem.class));
    }

    @Test
    void deleteInvoiceItemReturnsEmptyWhenInvoiceMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.empty());
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertTrue(service.deleteInvoiceItem(77L, 55L, principal).isEmpty());
    }

    @Test
    void deleteInvoiceItemThrowsWhenNotEditable() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertThrows(ResponseStatusException.class, () -> service.deleteInvoiceItem(77L, 55L, principal));
    }

    @Test
    void deleteInvoiceItemReturnsEmptyWhenItemMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByIdAndInvoiceId(55L, invoice.getId())).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertTrue(service.deleteInvoiceItem(77L, 55L, principal).isEmpty());
    }

    @Test
    void deleteInvoiceItemDeletesAndRecalculatesTotals() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceItemRepository invoiceItemRepository = mock(InvoiceItemRepository.class);
        InvoiceItemService service = new InvoiceItemService(invoiceRepository, invoiceItemRepository);

        Invoice invoice = new Invoice(new Company(), new Customer(new Company()));
        invoice.setStatus(InvoiceStatus.DRAFT);
        InvoiceItem item = new InvoiceItem(invoice);
        when(invoiceRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByIdAndInvoiceId(55L, invoice.getId())).thenReturn(Optional.of(item));
        when(invoiceItemRepository.findByInvoiceIdOrderByPositionAsc(invoice.getId()))
                .thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertTrue(service.deleteInvoiceItem(77L, 55L, principal).isPresent());
        verify(invoiceItemRepository).delete(any(InvoiceItem.class));
        verify(invoiceRepository).save(any(Invoice.class));
    }
}
