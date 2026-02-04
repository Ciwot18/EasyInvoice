package com.kernith.easyinvoice.data.dto.invoiceitem;

import com.kernith.easyinvoice.data.model.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateInvoiceItemRequest(
        @NotNull Integer position,
        @NotBlank @Size(max = 1000) String description,
        @Size(max = 1000) String notes,
        BigDecimal quantity,
        @Size(max = 20) String unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        DiscountType discountType,
        BigDecimal discountValue
) {}
