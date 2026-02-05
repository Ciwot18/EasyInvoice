package com.kernith.easyinvoice.helper.adapter;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;

import java.util.List;

import static com.kernith.easyinvoice.helper.Utils.money;
import static com.kernith.easyinvoice.helper.Utils.nvl;

public class QuotePdfAdapter implements PdfDocumentView {
    private final Quote quote;
    private final List<QuoteItem> items;
    private final Company company;
    private final Customer customer;

    public QuotePdfAdapter(Quote quote, List<QuoteItem> items, Company company, Customer customer) {
        this.quote = quote;
        this.items = items;
        this.company = company;
        this.customer = customer;
    }

    @Override
    public String title() { return "Preventivo"; }
    @Override
    public String numberLabel() {
        return quote.getQuoteYear() + "/" + quote.getQuoteNumber();
    }
    @Override
    public String statusLabel() { return quote.getStatus().name(); }
    @Override
    public String issueDateLabel() { return String.valueOf(quote.getIssueDate()); }
    @Override
    public String dueDateLabel() {
        return quote.getValidUntil() == null ? "" : String.valueOf(quote.getValidUntil());
    }
    public String companyVAT() { return quote.getCompany().getVatNumber(); }
    @Override
    public String customerVAT() { return quote.getCustomer().getVatNumber(); }
    @Override
    public String customerEmail() {
        String email = nvl(quote.getCustomer().getEmail());
        String pec = nvl(quote.getCustomer().getPec());
        if (!email.isBlank()) {
            return email;
        }
        if (!pec.isBlank()) {
            return pec;
        }
        return "";
    }
    @Override
    public String companyName() { return company.getName(); }
    @Override
    public String customerName() { return customer.getLegalName(); }
    @Override
    public String currency() { return quote.getCurrency(); }
    @Override
    public String notes() { return nvl(quote.getNotes()); }
    @Override
    public String subtotalLabel() { return money(quote.getSubtotalAmount(), quote.getCurrency()); }
    @Override
    public String taxLabel() { return money(quote.getTaxAmount(), quote.getCurrency()); }
    @Override
    public String totalLabel() { return money(quote.getTotalAmount(), quote.getCurrency()); }
    @Override
    public List<? extends PdfLineView> lines() {
        String currency = quote.getCurrency();
        return items.stream().map(item -> new QuoteLineAdapter(item, currency)).toList();
    }
}
