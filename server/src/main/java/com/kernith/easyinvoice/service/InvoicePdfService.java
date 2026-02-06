package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDownload;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDto;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoicePdfArchive;
import com.kernith.easyinvoice.data.repository.InvoicePdfArchiveRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles persistence and retrieval of generated invoice PDFs.
 */
@Service
public class InvoicePdfService {
    private final InvoicePdfArchiveRepository archiveRepository;
    private final InvoiceRepository invoiceRepository;
    private final PdfService pdfService;
    private final Path storageRoot;

    /**
     * Creates the service with required repositories and storage configuration.
     *
     * @param archiveRepository PDF archive repository
     * @param invoiceRepository invoice repository
     * @param pdfService PDF generator service
     * @param storageRoot storage root path
     */
    public InvoicePdfService(
            InvoicePdfArchiveRepository archiveRepository,
            InvoiceRepository invoiceRepository,
            PdfService pdfService,
            @Value("${storage.root:storage}") String storageRoot
    ) {
        this.archiveRepository = archiveRepository;
        this.invoiceRepository = invoiceRepository;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.pdfService = pdfService;
    }

    /**
     * Lists saved PDF versions for a given invoice.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return list of PDF metadata
     * @throws ResponseStatusException if the invoice is not accessible
     */
    @Transactional(readOnly = true)
    public List<InvoicePdfDto> listVersions(Long invoiceId, AuthPrincipal principal) {
        getRequiredInvoice(invoiceId, principal);

        return archiveRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(e -> new InvoicePdfDto(e.getId(), e.getFileName(), e.getCreatedAt()))
                .toList();
    }

    /**
     * Loads a saved PDF version as a downloadable resource.
     *
     * @param invoiceId invoice identifier
     * @param saveId PDF archive identifier
     * @param principal authenticated principal
     * @return file name and resource
     * @throws IllegalArgumentException if the PDF metadata is missing
     * @throws IllegalStateException if the PDF file is not readable on disk
     */
    @Transactional(readOnly = true)
    public InvoicePdfDownload download(Long invoiceId, Long saveId, AuthPrincipal principal) {
        getRequiredInvoice(invoiceId, principal);

        InvoicePdfArchive entity = archiveRepository.findByIdAndInvoiceId(saveId, invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("PDF version not found"));

        try {
            Path filePath = resolveStoragePath(entity.getPath(), entity.getFileName());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IOException("PDF file not readable");
            }
            return new InvoicePdfDownload(entity.getFileName(), resource);
        } catch (IOException e) {
            throw new IllegalStateException("PDF file missing on storage", e);
        }
    }

    /**
     * Generates and stores the issued invoice PDF, then archives metadata.
     *
     * <p>Lifecycle: validate invoice and ownership, build filename, generate PDF,
     * write to storage, then create an archive row.</p>
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return saved archive entity
     * @throws ResponseStatusException if invoice or customer is invalid
     * @throws IllegalStateException if the PDF cannot be stored
     */
    @Transactional
    public InvoicePdfArchive saveIssuedPdf(Long invoiceId, AuthPrincipal principal) {
        Long companyId = getRequiredCompanyId(principal);
        Invoice invoice = getRequiredInvoice(invoiceId, companyId);
        Long customerId = getRequiredCustomerId(invoice);

        LocalDateTime now = LocalDateTime.now();
        String relativeDir = "companies/" + companyId + "/customers/" + customerId + "/invoices/" + invoiceId;

        String fileName = buildUniqueFileName(invoiceId, now);
        byte[] pdfBytes = pdfService.invoicePdf(invoiceId, principal);
        try {
            Path dirPath = resolveStorageDirectory(relativeDir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName).normalize();
            if (!filePath.startsWith(dirPath)) {
                throw new IOException("Invalid storage path");
            }
            Files.write(filePath, pdfBytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store PDF", e);
        }

        return archiveRepository.save(new InvoicePdfArchive(invoice, relativeDir, fileName));
    }

    /**
     * Builds a unique filename for an invoice PDF.
     *
     * @param invoiceId invoice identifier
     * @param now timestamp used in the filename
     * @return unique file name
     */
    private String buildUniqueFileName(Long invoiceId, LocalDateTime now) {
        String ts = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String rand = java.util.UUID.randomUUID().toString().substring(0, 6);
        return "INV_" + invoiceId + "_" + ts + "_" + rand + ".pdf";
    }

    /**
     * Loads an invoice and validates ownership from the principal.
     *
     * @param invoiceId invoice identifier
     * @param principal authenticated principal
     * @return invoice entity
     */
    private Invoice getRequiredInvoice(Long invoiceId, AuthPrincipal principal) {
        Long companyId = getRequiredCompanyId(principal);
        return getRequiredInvoice(invoiceId, companyId);
    }

    /**
     * Loads an invoice by id and company.
     *
     * @param invoiceId invoice identifier
     * @param companyId company identifier
     * @return invoice entity
     * @throws ResponseStatusException if not found
     */
    private Invoice getRequiredInvoice(Long invoiceId, Long companyId) {
        return invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters"));
    }

    /**
     * Ensures the invoice has a valid customer reference.
     *
     * @param invoice invoice entity
     * @return customer id
     * @throws ResponseStatusException if missing
     */
    private Long getRequiredCustomerId(Invoice invoice) {
        if (invoice == null || invoice.getCustomer() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing customer");
        }
        Long customerId = invoice.getCustomer().getId();
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing customer");
        }
        return customerId;
    }

    /**
     * Ensures the principal and company id are present.
     *
     * @param principal authenticated principal
     * @return company id
     * @throws ResponseStatusException if missing
     */
    private Long getRequiredCompanyId(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        Long companyId = principal.companyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing company");
        }
        return companyId;
    }

    /**
     * Resolves and validates a storage directory under the storage root.
     *
     * @param relativeDir relative directory
     * @return normalized directory path
     * @throws IOException if the path is invalid
     */
    private Path resolveStorageDirectory(String relativeDir) throws IOException {
        Path dirPath = storageRoot.resolve(relativeDir).normalize();
        if (!dirPath.startsWith(storageRoot)) {
            throw new IOException("Invalid storage directory");
        }
        return dirPath;
    }

    /**
     * Resolves and validates a file path under the storage root.
     *
     * @param relativeDir relative directory
     * @param fileName file name
     * @return normalized file path
     * @throws IOException if the path is invalid
     */
    private Path resolveStoragePath(String relativeDir, String fileName) throws IOException {
        Path dirPath = resolveStorageDirectory(relativeDir);
        Path filePath = dirPath.resolve(fileName).normalize();
        if (!filePath.startsWith(dirPath)) {
            throw new IOException("Invalid storage path");
        }
        return filePath;
    }
}
