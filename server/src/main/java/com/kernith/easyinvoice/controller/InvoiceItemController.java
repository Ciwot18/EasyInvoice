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

@RestController
public class InvoiceItemController {

    private final InvoiceItemService invoiceItemService;

    public InvoiceItemController(InvoiceItemService invoiceItemService) {
        this.invoiceItemService = invoiceItemService;
    }

    @PostMapping("/invoices/{invoiceId}/items")
    public ResponseEntity<InvoiceItemResponse> addInvoiceItem(
            @PathVariable("invoiceId") Long invoiceId,
            @Valid @RequestBody CreateInvoiceItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceItemResponse.from(invoiceItemService.addInvoiceItem(invoiceId, request, principal)));
    }

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

    @PatchMapping("/invoices/{invoiceId}/items/{itemId}")
    public ResponseEntity<InvoiceItemResponse> updateInvoiceItem(
            @PathVariable("invoiceId") Long invoiceId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateInvoiceItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(InvoiceItemResponse.from(invoiceItemService.updateInvoiceItem(invoiceId, itemId, request, principal)));
    }

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