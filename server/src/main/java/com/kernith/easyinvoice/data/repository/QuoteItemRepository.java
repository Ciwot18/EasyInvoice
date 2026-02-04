package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.QuoteItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {

    List<QuoteItem> findByQuoteIdOrderByPositionAsc(Long quoteId);

    Optional<QuoteItem> findByIdAndQuoteId(Long id, Long quoteId);
}