package com.kernith.easyinvoice.helper.adapter;

/**
 * View model for a PDF document header, totals, and lines.
 */
public interface PdfDocumentView {
    /**
     * Title displayed in the header (e.g., Invoice, Quote).
     *
     * @return document title
     */
    String title();
    /**
     * Human-readable document number.
     *
     * @return document number label
     */
    String numberLabel();
    /**
     * Human-readable document status.
     *
     * @return status label
     */
    String statusLabel();
    /**
     * Issue date formatted for display.
     *
     * @return issue date label
     */
    String issueDateLabel();
    /**
     * Due or valid-until date formatted for display.
     *
     * @return due date label
     */
    String dueDateLabel();

    /**
     * Company legal name.
     *
     * @return company name
     */
    String companyName();
    /**
     * Customer legal name.
     *
     * @return customer name
     */
    String customerName();
    /**
     * Company VAT number.
     *
     * @return company VAT number
     */
    String companyVAT();
    /**
     * Customer VAT number.
     *
     * @return customer VAT number
     */
    String customerVAT();
    /**
     * Customer email or PEC.
     *
     * @return customer email or PEC
     */
    String customerEmail();

    /**
     * Currency code used in the document.
     *
     * @return currency code
     */
    String currency();
    /**
     * Optional notes section.
     *
     * @return notes text
     */
    String notes();

    /**
     * Subtotal formatted for display.
     *
     * @return subtotal label
     */
    String subtotalLabel();
    /**
     * Tax total formatted for display.
     *
     * @return tax total label
     */
    String taxLabel();
    /**
     * Grand total formatted for display.
     *
     * @return grand total label
     */
    String totalLabel();

    /**
     * Lines to render in the document body.
     *
     * @return list of line views
     */
    java.util.List<? extends PdfLineView> lines();
}
