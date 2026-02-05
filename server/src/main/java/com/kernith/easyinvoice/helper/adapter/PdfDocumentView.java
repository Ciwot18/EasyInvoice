package com.kernith.easyinvoice.helper.adapter;

public interface PdfDocumentView {
    String title();
    String numberLabel();
    String statusLabel();
    String issueDateLabel();
    String dueDateLabel();

    String companyName();
    String customerName();
    String companyVAT();
    String customerVAT();
    String customerEmail();

    String currency();
    String notes();

    String subtotalLabel();
    String taxLabel();
    String totalLabel();

    java.util.List<? extends PdfLineView> lines();
}