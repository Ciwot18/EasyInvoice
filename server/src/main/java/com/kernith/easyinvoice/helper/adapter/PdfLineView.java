package com.kernith.easyinvoice.helper.adapter;

public interface PdfLineView {
    int position();
    String description();
    String notes();

    String qtyLabel();
    String unitLabel();
    String unitPriceLabel();
    String taxRateLabel();

    String lineTotalLabel();
}