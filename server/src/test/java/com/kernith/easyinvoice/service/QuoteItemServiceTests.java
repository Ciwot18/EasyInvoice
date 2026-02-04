package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.UpdateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
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

class QuoteItemServiceTests {

    @Test
    void addQuoteItemCreatesAndRecalculatesTotals() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.save(any(QuoteItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId()))
                .thenReturn(List.of());
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateQuoteItemRequest req = new CreateQuoteItemRequest(
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

        QuoteItem saved = service.addQuoteItem(77L, req, principal);

        assertEquals("Item", saved.getDescription());
        assertEquals("Notes", saved.getNotes());
        verify(quoteItemRepository).save(any(QuoteItem.class));
        verify(quoteRepository).save(any(Quote.class));
    }

    @Test
    void addQuoteItemThrowsWhenNotEditable() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.SENT);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));

        CreateQuoteItemRequest req = new CreateQuoteItemRequest(
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

        assertThrows(ResponseStatusException.class, () -> service.addQuoteItem(77L, req, principal));
    }

    @Test
    void updateQuoteItemThrowsWhenMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.findByIdAndQuoteId(55L, quote.getId())).thenReturn(Optional.empty());

        UpdateQuoteItemRequest req = new UpdateQuoteItemRequest(2, "Updated", null, null, null, null, null, null, null);
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> service.updateQuoteItem(77L, 55L, req, principal));
    }

    @Test
    void updateQuoteItemUpdatesFields() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        QuoteItem item = new QuoteItem(quote);
        item.setDescription("Old");
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.findByIdAndQuoteId(55L, quote.getId())).thenReturn(Optional.of(item));
        when(quoteItemRepository.save(any(QuoteItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId()))
                .thenReturn(List.of(item));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateQuoteItemRequest req = new UpdateQuoteItemRequest(2, " Updated ", null, null, null, null, null, null, null);
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        QuoteItem updated = service.updateQuoteItem(77L, 55L, req, principal);
        assertEquals("Updated", updated.getDescription());
        verify(quoteItemRepository).save(any(QuoteItem.class));
    }

    @Test
    void deleteQuoteItemReturnsEmptyWhenQuoteMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.empty());
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());

        assertTrue(service.deleteQuoteItem(77L, 55L, principal).isEmpty());
    }

    @Test
    void deleteQuoteItemThrowsWhenNotEditable() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.ACCEPTED);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertThrows(ResponseStatusException.class, () -> service.deleteQuoteItem(77L, 55L, principal));
    }

    @Test
    void listQuoteItemsReturnsItems() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId()))
                .thenReturn(List.of(new QuoteItem(quote)));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        List<QuoteItem> items = service.listQuoteItems(77L, principal);

        assertEquals(1, items.size());
    }

    @Test
    void deleteQuoteItemReturnsEmptyWhenItemMissing() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.findByIdAndQuoteId(55L, quote.getId())).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertTrue(service.deleteQuoteItem(77L, 55L, principal).isEmpty());
    }

    @Test
    void deleteQuoteItemDeletesAndRecalculatesTotals() {
        QuoteRepository quoteRepository = mock(QuoteRepository.class);
        QuoteItemRepository quoteItemRepository = mock(QuoteItemRepository.class);
        QuoteItemService service = new QuoteItemService(quoteRepository, quoteItemRepository);

        Quote quote = new Quote(new Company(), new Customer(new Company()));
        quote.setStatus(QuoteStatus.DRAFT);
        QuoteItem item = new QuoteItem(quote);
        when(quoteRepository.findByIdAndCompanyId(77L, 10L)).thenReturn(Optional.of(quote));
        when(quoteItemRepository.findByIdAndQuoteId(55L, quote.getId())).thenReturn(Optional.of(item));
        when(quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId()))
                .thenReturn(List.of());
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
        assertTrue(service.deleteQuoteItem(77L, 55L, principal).isPresent());
        verify(quoteItemRepository).delete(any(QuoteItem.class));
        verify(quoteRepository).save(any(Quote.class));
    }
}
