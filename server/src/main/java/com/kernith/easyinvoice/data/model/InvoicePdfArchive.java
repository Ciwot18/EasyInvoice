package com.kernith.easyinvoice.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoice_pdf_archive",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_invoice_pdf_archive_path_filename",
                        columnNames = {"path", "file_name"}
                )
        },
        indexes = {
                @Index(name = "idx_invoice_pdf_archive_invoice_created", columnList = "invoice_id, created_at DESC"),
                @Index(name = "idx_invoice_pdf_archive_invoice", columnList = "invoice_id")
        }
)
public class InvoicePdfArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "path", nullable = false, length = 1024)
    private String path;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    protected InvoicePdfArchive() {}

    public InvoicePdfArchive(Invoice invoice, String path, String fileName) {
        this.invoice = invoice;
        this.path = path;
        this.fileName = fileName;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}