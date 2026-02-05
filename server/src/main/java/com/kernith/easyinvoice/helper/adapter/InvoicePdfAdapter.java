package com.kernith.easyinvoice.helper.adapter;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;

import java.util.List;

import static com.kernith.easyinvoice.helper.Utils.money;
import static com.kernith.easyinvoice.helper.Utils.nvl;

public final class InvoicePdfAdapter implements PdfDocumentView {

    private final Invoice invoice;
    private final List<InvoiceItem> items;
    private final Company company;
    private final Customer customer;

    public InvoicePdfAdapter(Invoice invoice, List<InvoiceItem> items, Company company, Customer customer) {
        this.invoice = invoice;
        this.items = items;
        this.company = company;
        this.customer = customer;
    }

    @Override
    public String title() { return "Fattura"; }
    @Override
    public String numberLabel() { return invoice.getInvoiceYear() + "/" + invoice.getInvoiceNumber(); }
    @Override
    public String statusLabel() { return invoice.getStatus().name(); }
    @Override
    public String issueDateLabel() { return String.valueOf(invoice.getIssueDate()); }
    @Override
    public String dueDateLabel() { return invoice.getDueDate() == null ? "" : String.valueOf(invoice.getDueDate()); }
    @Override
    public String companyVAT() { return invoice.getCompany().getVatNumber(); }
    @Override
    public String customerVAT() { return invoice.getCustomer().getVatNumber(); }
    @Override
    public String customerEmail() {
        String email = invoice.getCustomer().getEmail();
        String pec = invoice.getCustomer().getPec();
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
    public String currency() { return invoice.getCurrency(); }
    @Override
    public String notes() { return nvl(invoice.getNotes()); }

    @Override
    public String subtotalLabel() { return money(invoice.getSubtotalAmount(), invoice.getCurrency()); }
    @Override
    public String taxLabel() { return money(invoice.getTaxAmount(), invoice.getCurrency()); }
    @Override
    public String totalLabel() { return money(invoice.getTotalAmount(), invoice.getCurrency()); }

    @Override public List<? extends PdfLineView> lines() {
        String currency = invoice.getCurrency();
        return items.stream().map(item -> new InvoiceLineAdapter(item, currency)).toList();
    }
}