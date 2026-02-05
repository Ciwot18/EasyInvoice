package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.InvoiceItemResponse;
import com.kernith.easyinvoice.data.dto.invoiceitem.UpdateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.InvoiceItemService;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Invoice item endpoints for managing items within an invoice.
 */
@RestController
public class InvoiceItemController {

    private final InvoiceItemService invoiceItemService;

    public InvoiceItemController(InvoiceItemService invoiceItemService) {
        this.invoiceItemService = invoiceItemService;
    }

    /**
     * Adds an item to an invoice.
     *
     * @param invoiceId invoice identifier
     * @param request item creation payload
     * @param principal authenticated principal
     * @return created item response
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/invoices/{invoiceId}/items")
    public ResponseEntity<InvoiceItemResponse> addInvoiceItem(
            @PathVariable("invoiceId") Long invoiceId,
            @Valid @RequestBody CreateInvoiceItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceItemResponse.from(invoiceItemService.addInvoiceItem(invoiceId, request, principal)));
    }

    /**
     * Lists all items for an invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return list of items or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/invoices/{invoiceId}/items")
    public ResponseEntity<List<InvoiceItemResponse>> listInvoiceItems(
            @PathVariable("invoiceId") Long invoiceId,
            @CurrentUser AuthPrincipal principal
    ) {
        List<InvoiceItem> items = invoiceItemService.listInvoiceItems(invoiceId, principal);
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(items.stream().map(InvoiceItemResponse::from).toList());
    }

    /**
     * Updates an existing invoice item.
     *
     * @param invoiceId invoice identifier
     * @param itemId item identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return updated item response
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PatchMapping("/invoices/{invoiceId}/items/{itemId}")
    public ResponseEntity<InvoiceItemResponse> updateInvoiceItem(
            @PathVariable("invoiceId") Long invoiceId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateInvoiceItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceItemResponse.from(invoiceItemService.updateInvoiceItem(invoiceId, itemId, request, principal)));
    }

    /**
     * Deletes an item from an invoice.
     *
     * @param invoiceId invoice identifier
     * @param itemId item identifier
     * @param principal authenticated principal
     * @return {@code 204 No Content} on success or {@code 400 Bad Request} on failure
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @DeleteMapping("/invoices/{invoiceId}/items/{itemId}")
    public ResponseEntity<Void> deleteInvoiceItem(
            @PathVariable("invoiceId") Long invoiceId,
            @PathVariable("itemId") Long itemId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (invoiceItemService.deleteInvoiceItem(invoiceId, itemId, principal).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }
}
