package com.kernith.easyinvoice.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "quote_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_quote_items_quote_position", columnNames = {"quote_id", "position"})
        },
        indexes = {
                @Index(name = "idx_quote_items_quote_id", columnList = "quote_id")
        }
)
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType = DiscountType.NONE;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(name = "line_subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineSubtotalAmount = BigDecimal.ZERO;

    @Column(name = "line_tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTaxAmount = BigDecimal.ZERO;

    @Column(name = "line_total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime updatedAt;

    public QuoteItem() {}

    public QuoteItem(Quote quote) {
        this.quote = quote;
    }

    public QuoteItem(
            Quote quote,
            Integer position,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            DiscountType discountType,
            BigDecimal discountValue
    ) {
        this.quote = quote;
        this.position = position;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.discountType = discountType;
        this.discountValue = discountValue;
        recalculateLineAmounts();
    }

    public Long getId() {
        return id;
    }

    public Quote getQuote() {
        return quote;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        recalculateLineAmounts();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateLineAmounts();
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        recalculateLineAmounts();
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
        recalculateLineAmounts();
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
        recalculateLineAmounts();
    }

    public BigDecimal getLineSubtotalAmount() {
        recalculateLineAmounts();
        return lineSubtotalAmount;
    }

    public BigDecimal getLineTaxAmount() {
        recalculateLineAmounts();
        return lineTaxAmount;
    }

    public BigDecimal getLineTotalAmount() {
        recalculateLineAmounts();
        return lineTotalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private void recalculateLineAmounts() {
        BigDecimal effectiveQuantity = quantity == null ? BigDecimal.ONE : quantity;
        BigDecimal effectiveUnitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        BigDecimal effectiveTaxRate = taxRate == null ? BigDecimal.ZERO : taxRate;

        BigDecimal lineSubtotal = effectiveQuantity.multiply(effectiveUnitPrice);
        BigDecimal discountedSubtotal = applyDiscount(lineSubtotal);
        BigDecimal lineTax = discountedSubtotal.multiply(effectiveTaxRate).divide(BigDecimal.valueOf(100));
        BigDecimal lineTotal = discountedSubtotal.add(lineTax);

        this.lineSubtotalAmount = discountedSubtotal;
        this.lineTaxAmount = lineTax;
        this.lineTotalAmount = lineTotal;
    }

    private BigDecimal applyDiscount(BigDecimal lineSubtotal) {
        if (lineSubtotal == null) {
            return null;
        }
        DiscountType effectiveType = discountType == null ? DiscountType.NONE : discountType;
        if (effectiveType == DiscountType.NONE) {
            return lineSubtotal;
        }

        BigDecimal discountedSubtotal = lineSubtotal;
        if (effectiveType == DiscountType.PERCENT) {
            BigDecimal percent = discountValue == null ? BigDecimal.ZERO : discountValue;
            BigDecimal discountAmount = lineSubtotal.multiply(percent).divide(BigDecimal.valueOf(100));
            discountedSubtotal = lineSubtotal.subtract(discountAmount);
        } else if (effectiveType == DiscountType.AMOUNT) {
            BigDecimal amount = discountValue == null ? BigDecimal.ZERO : discountValue;
            discountedSubtotal = lineSubtotal.subtract(amount);
        }

        if (discountedSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return discountedSubtotal;
    }
}
