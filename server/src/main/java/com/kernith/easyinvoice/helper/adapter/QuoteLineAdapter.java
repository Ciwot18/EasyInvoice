package com.kernith.easyinvoice.helper.adapter;

import com.kernith.easyinvoice.data.model.QuoteItem;

import static com.kernith.easyinvoice.helper.Utils.*;

/**
 * Adapts {@link QuoteItem} data to {@link PdfLineView} for quote PDFs.
 */
public class QuoteLineAdapter implements PdfLineView {
    private final QuoteItem it;
    private final String currency;

    /**
     * Creates a line adapter for a quote item.
     *
     * @param it quote item
     * @param currency currency code
     */
    protected QuoteLineAdapter(QuoteItem it, String currency) {
        this.it = it;
        this.currency = currency;
    }

    @Override
    public int position() { return it.getPosition(); }
    @Override
    public String description() { return it.getDescription(); }
    @Override
    public String notes() { return nvl(it.getNotes()); }
    @Override
    public String qtyLabel() { return num4(it.getQuantity()); }
    @Override
    public String unitLabel() { return nvl(it.getUnit()); }
    @Override
    public String unitPriceLabel() { return money4(it.getUnitPrice(), currency); }
    @Override
    public String taxRateLabel() { return percent2(it.getTaxRate()); }
    @Override
    public String lineTotalLabel() { return money(it.getLineTotalAmount(), currency); }
}
