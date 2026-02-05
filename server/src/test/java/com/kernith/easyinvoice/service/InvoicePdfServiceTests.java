package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDownload;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDto;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoicePdfArchive;
import com.kernith.easyinvoice.data.repository.InvoicePdfArchiveRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoicePdfServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void listVersionsReturnsDtos() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfArchiveRepository archiveRepository = mock(InvoicePdfArchiveRepository.class);
        PdfService pdfService = mock(PdfService.class);
        InvoicePdfService service = new InvoicePdfService(
                archiveRepository,
                invoiceRepository,
                pdfService,
                tempDir.toString()
        );

        Invoice invoice = buildInvoice(10L, 20L, 30L);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));

        InvoicePdfArchive archive = new InvoicePdfArchive(invoice, "companies/10", "INV_30.pdf");
        ReflectionTestUtils.setField(archive, "id", 55L);
        ReflectionTestUtils.setField(archive, "createdAt", LocalDateTime.of(2025, 1, 10, 9, 30));
        when(archiveRepository.findByInvoiceIdOrderByCreatedAtDesc(30L)).thenReturn(List.of(archive));

        List<InvoicePdfDto> result = service.listVersions(30L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of()));

        assertEquals(1, result.size());
        assertEquals(55L, result.get(0).id());
        assertEquals("INV_30.pdf", result.get(0).fileName());
    }

    @Test
    void listVersionsThrowsWhenInvoiceMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfService service = new InvoicePdfService(
                mock(InvoicePdfArchiveRepository.class),
                invoiceRepository,
                mock(PdfService.class),
                tempDir.toString()
        );

        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.listVersions(30L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of())));
    }

    @Test
    void downloadReturnsResourceWhenFileExists() throws Exception {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfArchiveRepository archiveRepository = mock(InvoicePdfArchiveRepository.class);
        PdfService pdfService = mock(PdfService.class);
        InvoicePdfService service = new InvoicePdfService(
                archiveRepository,
                invoiceRepository,
                pdfService,
                tempDir.toString()
        );

        Invoice invoice = buildInvoice(10L, 20L, 30L);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));

        Path dir = tempDir.resolve("companies/10/customers/20/invoices/30");
        Files.createDirectories(dir);
        Path file = dir.resolve("INV_30.pdf");
        Files.write(file, "pdf".getBytes());

        InvoicePdfArchive archive = new InvoicePdfArchive(invoice, "companies/10/customers/20/invoices/30", "INV_30.pdf");
        when(archiveRepository.findByIdAndInvoiceId(55L, 30L)).thenReturn(Optional.of(archive));

        InvoicePdfDownload download = service.download(30L, 55L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of()));

        assertEquals("INV_30.pdf", download.fileName());
        assertTrue(download.resource().exists());
    }

    @Test
    void downloadThrowsWhenVersionMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfArchiveRepository archiveRepository = mock(InvoicePdfArchiveRepository.class);
        PdfService pdfService = mock(PdfService.class);
        InvoicePdfService service = new InvoicePdfService(
                archiveRepository,
                invoiceRepository,
                pdfService,
                tempDir.toString()
        );

        Invoice invoice = buildInvoice(10L, 20L, 30L);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));
        when(archiveRepository.findByIdAndInvoiceId(55L, 30L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.download(30L, 55L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of())));
    }

    @Test
    void downloadThrowsWhenFileMissing() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfArchiveRepository archiveRepository = mock(InvoicePdfArchiveRepository.class);
        PdfService pdfService = mock(PdfService.class);
        InvoicePdfService service = new InvoicePdfService(
                archiveRepository,
                invoiceRepository,
                pdfService,
                tempDir.toString()
        );

        Invoice invoice = buildInvoice(10L, 20L, 30L);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));

        InvoicePdfArchive archive = new InvoicePdfArchive(invoice, "companies/10/customers/20/invoices/30", "INV_30.pdf");
        when(archiveRepository.findByIdAndInvoiceId(55L, 30L)).thenReturn(Optional.of(archive));

        assertThrows(IllegalStateException.class,
                () -> service.download(30L, 55L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of())));
    }

    @Test
    void saveIssuedPdfStoresFileAndArchive() throws Exception {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfArchiveRepository archiveRepository = mock(InvoicePdfArchiveRepository.class);
        PdfService pdfService = mock(PdfService.class);
        InvoicePdfService service = new InvoicePdfService(
                archiveRepository,
                invoiceRepository,
                pdfService,
                tempDir.toString()
        );

        Invoice invoice = buildInvoice(10L, 20L, 30L);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));
        when(pdfService.invoicePdf(eq(30L), any(AuthPrincipal.class))).thenReturn("pdf".getBytes());
        when(archiveRepository.save(any(InvoicePdfArchive.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoicePdfArchive saved = service.saveIssuedPdf(30L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of()));

        assertNotNull(saved);
        assertTrue(Files.exists(tempDir.resolve(saved.getPath()).resolve(saved.getFileName())));
        verify(archiveRepository).save(any(InvoicePdfArchive.class));
    }

    @Test
    void saveIssuedPdfThrowsWhenMissingCustomer() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoicePdfService service = new InvoicePdfService(
                mock(InvoicePdfArchiveRepository.class),
                invoiceRepository,
                mock(PdfService.class),
                tempDir.toString()
        );

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 10L);
        Invoice invoice = new Invoice(company, null);
        when(invoiceRepository.findByIdAndCompanyId(30L, 10L)).thenReturn(Optional.of(invoice));

        assertThrows(ResponseStatusException.class,
                () -> service.saveIssuedPdf(30L, new AuthPrincipal(1L, 10L, "COMPANY_MANAGER", List.of())));
    }

    private Invoice buildInvoice(Long companyId, Long customerId, Long invoiceId) {
        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", companyId);
        Customer customer = new Customer(company);
        ReflectionTestUtils.setField(customer, "id", customerId);
        Invoice invoice = new Invoice(company, customer);
        ReflectionTestUtils.setField(invoice, "id", invoiceId);
        return invoice;
    }
}
