package com.kernith.easyinvoice.data.dto.invoiceitem;

import com.kernith.easyinvoice.data.model.DiscountType;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateInvoiceItemRequest(
        Integer position,
        @Size(max = 1000) String description,
        @Size(max = 1000) String notes,
        BigDecimal quantity,
        @Size(max = 20) String unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        DiscountType discountType,
        BigDecimal discountValue
) {}
