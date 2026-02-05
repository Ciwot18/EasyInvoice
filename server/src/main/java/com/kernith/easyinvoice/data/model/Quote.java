package com.kernith.easyinvoice.data.model;

import com.kernith.easyinvoice.data.model.state.quote.QuoteState;
import com.kernith.easyinvoice.data.model.state.quote.QuoteStateFactory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quote aggregate root with state-driven status transitions and total calculations.
 */
@Entity
@Table(
        name = "quotes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_quotes_company_year_number",
                        columnNames = {"company_id", "quote_year", "quote_number"}
                )
        },
        indexes = {
                @Index(name = "idx_quotes_company_id", columnList = "company_id"),
                @Index(name = "idx_quotes_customer_id", columnList = "customer_id"),
                @Index(name = "idx_quotes_company_status", columnList = "company_id, status"),
                @Index(name = "idx_quotes_company_issue_date", columnList = "company_id, issue_date")
        }
)
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @Transient
    private QuoteState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "quote", fetch = FetchType.LAZY)
    // private List<QuoteItem> items; Never assigned (But it is from JPA)
    private List<QuoteItem> items = new ArrayList<>();

    @Column(name = "quote_year", nullable = false)
    private Integer quoteYear;

    @Column(name = "quote_number", nullable = false)
    private Integer quoteNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "valid_until")
    private LocalDate validUntil;

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

    public Quote() {}

    public Quote(Company company, Customer customer) {
        this.company = company;
        this.customer = customer;
    }

    /**
     * Initializes the state handler after the entity is loaded.
     */
    @PostLoad
    void initState() {
        this.state = QuoteStateFactory.from(status);
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

    public List<QuoteItem> getItems() {
        return items;
    }

    public Integer getQuoteYear() {
        return quoteYear;
    }

    public void setQuoteYear(Integer quoteYear) {
        this.quoteYear = quoteYear;
    }

    public Integer getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(Integer quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    /**
     * Updates the status and refreshes the internal state handler.
     *
     * @param status new status
     */
    public void setStatus(QuoteStatus status) {
        this.status = status;
        if (status != null) {
            this.state = QuoteStateFactory.from(status);
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

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the subtotal amount, recalculating from items if needed.
     *
     * @return subtotal amount
     */
    public BigDecimal getSubtotalAmount() {
        recalculateTotalsFromItems(items);
        return subtotalAmount;
    }

    /**
     * Returns the tax amount, recalculating from items if needed.
     *
     * @return tax amount
     */
    public BigDecimal getTaxAmount() {
        recalculateTotalsFromItems(items);
        return taxAmount;
    }

    /**
     * Returns the total amount, recalculating from items if needed.
     *
     * @return total amount
     */
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

    /**
     * Recalculates subtotal, tax, and total amounts from the given items list.
     *
     * @param items quote items used for totals
     */
    public void recalculateTotalsFromItems(List<QuoteItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        if (items != null) {
            for (QuoteItem item : items) {
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

    /**
     * Returns zero for null monetary values.
     *
     * @param value input amount
     * @return non-null amount
     */
    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Transitions the quote to DRAFT if allowed by the current state.
     */
    public void draft() {
        ensureState();
        state.draft(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to SENT if allowed by the current state.
     */
    public void send() {
        ensureState();
        state.send(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to ACCEPTED if allowed by the current state.
     */
    public void accept() {
        ensureState();
        state.accept(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to REJECTED if allowed by the current state.
     */
    public void reject() {
        ensureState();
        state.reject(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to EXPIRED if allowed by the current state.
     */
    public void expire() {
        ensureState();
        state.expire(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to CONVERTED if allowed by the current state.
     */
    public void convert() {
        ensureState();
        state.convert(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Transitions the quote to ARCHIVED if allowed by the current state.
     */
    public void archive() {
        ensureState();
        state.archive(this);
        this.state = QuoteStateFactory.from(status);
    }

    /**
     * Ensures a state handler is available before transitions.
     */
    private void ensureState() {
        if (state == null) state = QuoteStateFactory.from(status);
    }
}
