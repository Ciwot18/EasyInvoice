package com.kernith.easyinvoice.helper.adapter;

/**
 * View model for a single line item row in a PDF document.
 */
public interface PdfLineView {
    /**
     * Sort order for line rendering.
     *
     * @return line position for sorting
     */
    int position();
    /**
     * Main description of the line item.
     *
     * @return line description
     */
    String description();
    /**
     * Optional notes shown under the description.
     *
     * @return notes text or empty string
     */
    String notes();

    /**
     * Quantity label formatted for display.
     *
     * @return formatted quantity label
     */
    String qtyLabel();
    /**
     * Unit label formatted for display.
     *
     * @return formatted unit label
     */
    String unitLabel();
    /**
     * Unit price label formatted for display.
     *
     * @return formatted unit price label
     */
    String unitPriceLabel();
    /**
     * Tax rate label formatted for display.
     *
     * @return formatted tax rate label
     */
    String taxRateLabel();

    /**
     * Line total formatted for display.
     *
     * @return formatted line total label
     */
    String lineTotalLabel();
}
