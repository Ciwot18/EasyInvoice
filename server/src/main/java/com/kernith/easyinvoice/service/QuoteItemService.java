package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.UpdateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.QuoteItemRepository;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import com.kernith.easyinvoice.helper.Utils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manages quote items and keeps quote totals in sync.
 */
@Service
public class QuoteItemService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;

    /**
     * Creates the service with repositories.
     *
     * @param quoteRepository quote repository
     * @param quoteItemRepository quote item repository
     */
    public QuoteItemService(QuoteRepository quoteRepository, QuoteItemRepository quoteItemRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
    }

    /**
     * Adds a new item to a quote and recalculates totals.
     *
     * <p>Lifecycle: validate role and quote state, create item, save, then recalc totals.</p>
     *
     * @param quoteId quote identifier
     * @param request item creation payload
     * @param principal authenticated principal
     * @return saved quote item
     * @throws ResponseStatusException if role or quote state is invalid
     */
    public QuoteItem addQuoteItem(Long quoteId, CreateQuoteItemRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Quote quote = getEditableQuote(quoteId, principal);

        QuoteItem item = new QuoteItem(quote);
        item.setPosition(request.position());
        item.setDescription(Utils.normalizeRequired(request.description()));
        item.setNotes(Utils.trimToNull(request.notes()));
        item.setQuantity(Utils.defaultBigDecimal(request.quantity(), BigDecimal.ONE));
        item.setUnit(Utils.trimToNull(request.unit()));
        item.setUnitPrice(Utils.defaultBigDecimal(request.unitPrice(), BigDecimal.ZERO));
        item.setTaxRate(Utils.defaultBigDecimal(request.taxRate(), BigDecimal.ZERO));
        item.setDiscountType(Utils.mapDiscountType(request.discountType()));
        item.setDiscountValue(Utils.defaultBigDecimal(request.discountValue(), BigDecimal.ZERO));

        QuoteItem saved = quoteItemRepository.save(item);
        recalcQuoteTotals(quote);
        return saved;
    }

    /**
     * Lists items for a given quote.
     *
     * @param quoteId quote identifier
     * @param principal authenticated principal
     * @return list of quote items
     * @throws ResponseStatusException if role or quote state is invalid
     */
    public List<QuoteItem> listQuoteItems(Long quoteId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Quote quote = getQuoteOrThrow(quoteId, principal);
        return quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId());
    }

    /**
     * Updates an existing quote item and recalculates totals.
     *
     * <p>Lifecycle: validate role and quote state, update fields, save, then recalc totals.</p>
     *
     * @param quoteId quote identifier
     * @param itemId item identifier
     * @param request update payload
     * @param principal authenticated principal
     * @return saved quote item
     * @throws ResponseStatusException if role or quote state is invalid
     */
    public QuoteItem updateQuoteItem(Long quoteId, Long itemId, UpdateQuoteItemRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Quote quote = getEditableQuote(quoteId, principal);

        Optional<QuoteItem> optionalItem = quoteItemRepository.findByIdAndQuoteId(itemId, quote.getId());
        if (optionalItem.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameters not valid");
        }
        QuoteItem item = optionalItem.get();

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
            item.setDiscountType(request.discountType());
        }
        if (request.discountValue() != null) {
            item.setDiscountValue(request.discountValue());
        }

        QuoteItem saved = quoteItemRepository.save(item);
        recalcQuoteTotals(quote);
        return saved;
    }

    /**
     * Deletes a quote item and recalculates totals if found.
     *
     * @param quoteId quote identifier
     * @param itemId item identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if role or quote state is invalid
     */
    public Optional<Boolean> deleteQuoteItem(Long quoteId, Long itemId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(
                quoteId,
                Utils.getRequiredCompanyId(principal)
        );
        if (optionalQuote.isEmpty()) {
            return Optional.empty();
        }
        Quote quote = optionalQuote.get();
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote is not editable");
        }

        Optional<QuoteItem> optionalItem = quoteItemRepository.findByIdAndQuoteId(itemId, quote.getId());
        if (optionalItem.isEmpty()) {
            return Optional.empty();
        }
        quoteItemRepository.delete(optionalItem.get());
        recalcQuoteTotals(quote);
        return Optional.of(Boolean.TRUE);
    }

    private Quote getQuoteOrThrow(Long quoteId, AuthPrincipal principal) {
        Long companyId = Utils.getRequiredCompanyId(principal);
        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(quoteId, companyId);
        if (optionalQuote.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote is not editable");
        }
        return optionalQuote.get();
    }

    private Quote getEditableQuote(Long quoteId, AuthPrincipal principal) {
        Quote quote = getQuoteOrThrow(quoteId, principal);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote is not editable");
        }
        return quote;
    }

    private void recalcQuoteTotals(Quote quote) {
        List<QuoteItem> items = quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId());
        quote.recalculateTotalsFromItems(items);
        quoteRepository.save(quote);
    }

}
