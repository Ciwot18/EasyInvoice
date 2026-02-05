package com.kernith.easyinvoice.data.model;

import com.kernith.easyinvoice.data.model.state.invoice.InvoiceState;
import com.kernith.easyinvoice.data.model.state.invoice.InvoiceStateFactory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_invoices_company_year_number",
                        columnNames = {"company_id", "invoice_year", "invoice_number"}
                )
        },
        indexes = {
                @Index(name = "idx_invoices_company_id", columnList = "company_id"),
                @Index(name = "idx_invoices_customer_id", columnList = "customer_id"),
                @Index(name = "idx_invoices_source_quote_id", columnList = "source_quote_id"),
                @Index(name = "idx_invoices_company_status", columnList = "company_id, status"),
                @Index(name = "idx_invoices_company_issue_date", columnList = "company_id, issue_date")
        }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_quote_id")
    private Quote sourceQuote;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    // private List<InvoiceItem> items; Never assigned (But it is from JPA)
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(name = "invoice_year", nullable = false)
    private Integer invoiceYear;

    @Column(name = "invoice_number", nullable = false)
    private Integer invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime updatedAt;

    @Transient
    private InvoiceState state;

    public Invoice() {}

    public Invoice(Company company, Customer customer) {
        this.company = company;
        this.customer = customer;
    }

    public Invoice(Quote quote) {
        if (quote == null) {
            return;
        }
        this.company = quote.getCompany();
        this.customer = quote.getCustomer();
        this.sourceQuote = quote;
        this.title = quote.getTitle();
        this.notes = quote.getNotes();
        this.issueDate = LocalDate.now();
        this.currency = quote.getCurrency();

        if (quote.getItems() != null) {
            for (QuoteItem quoteItem : quote.getItems()) {
                if (quoteItem == null) {
                    continue;
                }
                this.items.add(new InvoiceItem(this, quoteItem));
            }
            recalculateTotalsFromItems(items);
        }
    }

    @PostLoad
    void initState() {
        this.state = InvoiceStateFactory.from(status);
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Quote getSourceQuote() {
        return sourceQuote;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public Integer getInvoiceYear() {
        return invoiceYear;
    }

    public void setInvoiceYear(Integer invoiceYear) {
        this.invoiceYear = invoiceYear;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
        if (status != null) {
            this.state = InvoiceStateFactory.from(status);
        } else {
            this.state = null;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSubtotalAmount() {
        recalculateTotalsFromItems(items);
        return subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        recalculateTotalsFromItems(items);
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        recalculateTotalsFromItems(items);
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void recalculateTotalsFromItems(List<InvoiceItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        if (items != null) {
            for (InvoiceItem item : items) {
                if (item == null) {
                    continue;
                }
                subtotal = subtotal.add(defaultAmount(item.getLineSubtotalAmount()));
                tax = tax.add(defaultAmount(item.getLineTaxAmount()));
                total = total.add(defaultAmount(item.getLineTotalAmount()));
            }
        }

        this.subtotalAmount = subtotal;
        this.taxAmount = tax;
        this.totalAmount = total;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public void draft() {
        ensureState();
        state.draft(this);
        this.state = InvoiceStateFactory.from(status);
    }

    public void issue() {
        ensureState();
        state.issue(this);
        this.state = InvoiceStateFactory.from(status);
    }

    public void pay() {
        ensureState();
        state.pay(this);
        this.state = InvoiceStateFactory.from(status);
    }

    public void overdue() {
        ensureState();
        state.overdue(this);
        this.state = InvoiceStateFactory.from(status);
    }

    public void archive() {
        ensureState();
        state.archive(this);
        this.state = InvoiceStateFactory.from(status);
    }

    private void ensureState() {
        if (state == null) {
            state = InvoiceStateFactory.from(status);
        }
    }
}
