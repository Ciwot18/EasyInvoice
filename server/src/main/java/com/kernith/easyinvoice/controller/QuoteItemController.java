package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.QuoteItemResponse;
import com.kernith.easyinvoice.data.dto.quoteitem.UpdateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.QuoteItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteItemController {

    private final QuoteItemService quoteItemService;

    public QuoteItemController(QuoteItemService quoteItemService) {
        this.quoteItemService = quoteItemService;
    }

    @PostMapping("/api/quotes/{quoteId}/items")
    public ResponseEntity<QuoteItemResponse> addQuoteItem(
            @PathVariable("quoteId") Long quoteId,
            @Valid @RequestBody CreateQuoteItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteItemResponse.from(quoteItemService.addQuoteItem(quoteId, request, principal)));
    }

    @GetMapping("/api/quotes/{quoteId}/items")
    public ResponseEntity<List<QuoteItemResponse>> listQuoteItems(
            @PathVariable("quoteId") Long quoteId,
            @CurrentUser AuthPrincipal principal
    ) {
        List<QuoteItem> items = quoteItemService.listQuoteItems(quoteId, principal);
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(items.stream().map(QuoteItemResponse::from).toList());
    }

    @PatchMapping("/api/quotes/{quoteId}/items/{itemId}")
    public ResponseEntity<QuoteItemResponse> updateQuoteItem(
            @PathVariable("quoteId") Long quoteId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateQuoteItemRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(QuoteItemResponse.from(quoteItemService.updateQuoteItem(quoteId, itemId, request, principal)));
    }

    @DeleteMapping("/api/quotes/{quoteId}/items/{itemId}")
    public ResponseEntity<Void> deleteQuoteItem(
            @PathVariable("quoteId") Long quoteId,
            @PathVariable("itemId") Long itemId,
            @CurrentUser AuthPrincipal principal
    ) {
        if (quoteItemService.deleteQuoteItem(quoteId, itemId, principal).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
