package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.UpdateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.QuoteItemDiscountType;
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

@Service
public class QuoteItemService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;

    public QuoteItemService(QuoteRepository quoteRepository, QuoteItemRepository quoteItemRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
    }

    public QuoteItem addQuoteItem(Long quoteId, CreateQuoteItemRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Quote quote = getEditableQuote(quoteId, principal);

        QuoteItem item = new QuoteItem(quote);
        item.setPosition(request.position());
        item.setDescription(normalizeRequired(request.description()));
        item.setNotes(trimToNull(request.notes()));
        item.setQuantity(defaultBigDecimal(request.quantity(), BigDecimal.ONE));
        item.setUnit(trimToNull(request.unit()));
        item.setUnitPrice(defaultBigDecimal(request.unitPrice(), BigDecimal.ZERO));
        item.setTaxRate(defaultBigDecimal(request.taxRate(), BigDecimal.ZERO));
        item.setDiscountType(request.discountType() == null ? QuoteItemDiscountType.NONE : request.discountType());
        item.setDiscountValue(defaultBigDecimal(request.discountValue(), BigDecimal.ZERO));

        QuoteItem saved = quoteItemRepository.save(item);
        recalcQuoteTotals(quote);
        return saved;
    }

    public List<QuoteItem> listQuoteItems(Long quoteId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Quote quote = getQuoteOrThrow(quoteId, principal);
        return quoteItemRepository.findByQuoteIdOrderByPositionAsc(quote.getId());
    }

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
            item.setDiscountType(request.discountType());
        }
        if (request.discountValue() != null) {
            item.setDiscountValue(request.discountValue());
        }

        QuoteItem saved = quoteItemRepository.save(item);
        recalcQuoteTotals(quote);
        return saved;
    }

    public Optional<Boolean> deleteQuoteItem(Long quoteId, Long itemId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Optional<Quote> optionalQuote = quoteRepository.findByIdAndCompanyId(quoteId, getRequiredCompanyId(principal));
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
        Long companyId = getRequiredCompanyId(principal);
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
}
