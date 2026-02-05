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

@Service
public class InvoicePdfService {
    private final InvoicePdfArchiveRepository archiveRepository;
    private final InvoiceRepository invoiceRepository;
    private final PdfService pdfService;
    private final Path storageRoot;

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

    @Transactional(readOnly = true)
    public List<InvoicePdfDto> listVersions(Long invoiceId, AuthPrincipal principal) {
        getRequiredInvoice(invoiceId, principal);

        return archiveRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(e -> new InvoicePdfDto(e.getId(), e.getFileName(), e.getCreatedAt()))
                .toList();
    }

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

    private String buildUniqueFileName(Long invoiceId, LocalDateTime now) {
        String ts = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String rand = java.util.UUID.randomUUID().toString().substring(0, 6);
        return "INV_" + invoiceId + "_" + ts + "_" + rand + ".pdf";
    }

    private Invoice getRequiredInvoice(Long invoiceId, AuthPrincipal principal) {
        Long companyId = getRequiredCompanyId(principal);
        return getRequiredInvoice(invoiceId, companyId);
    }

    private Invoice getRequiredInvoice(Long invoiceId, Long companyId) {
        return invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters"));
    }

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

    private Path resolveStorageDirectory(String relativeDir) throws IOException {
        Path dirPath = storageRoot.resolve(relativeDir).normalize();
        if (!dirPath.startsWith(storageRoot)) {
            throw new IOException("Invalid storage directory");
        }
        return dirPath;
    }

    private Path resolveStoragePath(String relativeDir, String fileName) throws IOException {
        Path dirPath = resolveStorageDirectory(relativeDir);
        Path filePath = dirPath.resolve(fileName).normalize();
        if (!filePath.startsWith(dirPath)) {
            throw new IOException("Invalid storage path");
        }
        return filePath;
    }
}
