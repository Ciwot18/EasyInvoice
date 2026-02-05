package com.kernith.easyinvoice.helper.adapter;

import com.kernith.easyinvoice.data.model.InvoiceItem;

import static com.kernith.easyinvoice.helper.Utils.*;

/**
 * Adapts {@link InvoiceItem} data to {@link PdfLineView} for invoice PDFs.
 */
public class InvoiceLineAdapter implements PdfLineView {
    private final InvoiceItem it;
    private final String currency;

    /**
     * Creates a line adapter for an invoice item.
     *
     * @param it invoice item
     * @param currency currency code
     */
    public InvoiceLineAdapter(InvoiceItem it, String currency) {
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
    public String qtyLabel() { return it.getQuantity().setScale(4).toPlainString(); }
    @Override
    public String unitLabel() { return nvl(it.getUnit()); }
    @Override
    public String unitPriceLabel() { return money4(it.getUnitPrice(), currency); }
    @Override
    public String taxRateLabel() { return it.getTaxRate().setScale(2).toPlainString() + "%"; }
    @Override
    public String lineTotalLabel() { return money(it.getLineTotalAmount(), currency); }
}
