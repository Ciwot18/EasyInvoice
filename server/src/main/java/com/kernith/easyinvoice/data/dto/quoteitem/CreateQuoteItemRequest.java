package com.kernith.easyinvoice.data.dto.quoteitem;

import com.kernith.easyinvoice.data.model.QuoteItemDiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateQuoteItemRequest(
        @NotNull Integer position,
        @NotBlank @Size(max = 1000) String description,
        @Size(max = 1000) String notes,
        BigDecimal quantity,
        @Size(max = 20) String unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        QuoteItemDiscountType discountType,
        BigDecimal discountValue
) {}
