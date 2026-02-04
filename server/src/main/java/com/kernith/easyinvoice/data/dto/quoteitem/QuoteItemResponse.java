package com.kernith.easyinvoice.data.dto.quoteitem;

import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.QuoteItemDiscountType;
import java.math.BigDecimal;

public record QuoteItemResponse(
        Long id,
        Long quoteId,
        Integer position,
        String description,
        String notes,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        QuoteItemDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal lineSubtotalAmount,
        BigDecimal lineTaxAmount,
        BigDecimal lineTotalAmount
) {
    public static QuoteItemResponse from(QuoteItem item) {
        return new QuoteItemResponse(
                item.getId(),
                item.getQuote() == null ? null : item.getQuote().getId(),
                item.getPosition(),
                item.getDescription(),
                item.getNotes(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getTaxRate(),
                item.getDiscountType(),
                item.getDiscountValue(),
                item.getLineSubtotalAmount(),
                item.getLineTaxAmount(),
                item.getLineTotalAmount()
        );
    }
}
