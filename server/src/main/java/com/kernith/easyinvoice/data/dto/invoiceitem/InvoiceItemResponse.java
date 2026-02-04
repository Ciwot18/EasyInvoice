package com.kernith.easyinvoice.data.dto.invoiceitem;

import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long id,
        Long invoiceId,
        Integer position,
        String description,
        String notes,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal lineSubtotalAmount,
        BigDecimal lineTaxAmount,
        BigDecimal lineTotalAmount
) {
    public static InvoiceItemResponse from(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getInvoice() == null ? null : item.getInvoice().getId(),
                item.getPosition(),
                item.getDescription(),
                item.getNotes(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getTaxRate(),
                mapDiscountType(item.getDiscountType()),
                item.getDiscountValue(),
                item.getLineSubtotalAmount(),
                item.getLineTaxAmount(),
                item.getLineTotalAmount()
        );
    }

    private static DiscountType mapDiscountType(DiscountType discountType) {
        if (discountType == null) {
            return null;
        }
        return DiscountType.valueOf(discountType.name());
    }
}
