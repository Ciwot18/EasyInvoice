package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.invoice.CreateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDownload;
import com.kernith.easyinvoice.data.dto.invoice.InvoicePdfDto;
import com.kernith.easyinvoice.data.dto.invoice.UpdateInvoiceRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoicePdfArchive;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.InvoicePdfService;
import com.kernith.easyinvoice.service.InvoiceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.kernith.easyinvoice.service.PdfService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        com.kernith.easyinvoice.helper.GlobalExceptionHandler.class
})
@ActiveProfiles("test")
class InvoiceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InvoiceService invoiceService;
    @MockitoBean
    private InvoicePdfService invoicePdfService;
    @MockitoBean
    private PdfService pdfService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setPrincipal() {
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );
    }

    @Nested
    class createInvoiceTests {
        @Test
        void createInvoiceReturnsOkWhenValid() throws Exception {
            setPrincipal();
            CreateInvoiceRequest req = new CreateInvoiceRequest(
                    100L,
                    null,
                    "Title",
                    "Notes",
                    null,
                    null,
                    "EUR",
                    List.of(new CreateInvoiceItemRequest(
                            1,
                            "Item",
                            null,
                            BigDecimal.ONE,
                            null,
                            BigDecimal.TEN,
                            BigDecimal.ZERO,
                            DiscountType.NONE,
                            BigDecimal.ZERO
                    ))
            );

            Invoice invoice = buildInvoice();
            when(invoiceService.createInvoice(any(CreateInvoiceRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(invoice);

            mockMvc.perform(post("/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L))
                    .andExpect(jsonPath("$.invoiceNumber").value(5))
                    .andExpect(jsonPath("$.invoiceYear").value(2025))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.customerDisplayName").value("Acme Spa"));
        }

        @Test
        void createInvoiceReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            CreateInvoiceRequest req = new CreateInvoiceRequest(
                    100L,
                    null,
                    "Title",
                    "Notes",
                    null,
                    null,
                    "EUR",
                    List.of()
            );

            mockMvc.perform(post("/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createInvoiceReturnsServerErrorWhenServiceFails() throws Exception {
            setPrincipal();
            CreateInvoiceRequest req = new CreateInvoiceRequest(
                    100L,
                    null,
                    "Title",
                    "Notes",
                    null,
                    null,
                    "EUR",
                    List.of(new CreateInvoiceItemRequest(
                            1,
                            "Item",
                            null,
                            BigDecimal.ONE,
                            null,
                            BigDecimal.TEN,
                            BigDecimal.ZERO,
                            DiscountType.NONE,
                            BigDecimal.ZERO
                    ))
            );
            when(invoiceService.createInvoice(any(CreateInvoiceRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(post("/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class listInvoicesTests {
        @Test
        void listInvoicesReturnsPageWhenNotEmpty() throws Exception {
            setPrincipal();
            Invoice invoice = buildInvoice();
            Page<Invoice> page = new PageImpl<>(List.of(invoice));
            when(invoiceService.listInvoices(any(AuthPrincipal.class), eq(0), eq(20), eq("issueDate,desc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(77L))
                    .andExpect(jsonPath("$.content[0].invoiceNumber").value(5));
        }

        @Test
        void listInvoicesReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            Page<Invoice> page = Page.empty();
            when(invoiceService.listInvoices(any(AuthPrincipal.class), eq(0), eq(20), eq("issueDate,desc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void listInvoicesReturnsServerErrorOnFailure() throws Exception {
            setPrincipal();
            when(invoiceService.listInvoices(any(AuthPrincipal.class), eq(0), eq(20), eq("issueDate,desc"), eq(null)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class getInvoiceTests {
        @Test
        void getInvoiceReturnsInvoiceWhenFound() throws Exception {
            setPrincipal();
            when(invoiceService.getInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(buildInvoice()));

            mockMvc.perform(get("/invoices/77"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L))
                    .andExpect(jsonPath("$.invoiceNumber").value(5));
        }

        @Test
        void getInvoiceReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(invoiceService.getInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/invoices/77"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getInvoiceReturnsServerErrorOnFailure() throws Exception {
            setPrincipal();
            when(invoiceService.getInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get("/invoices/77"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class pdfEndpointsTests {
        @Test
        void getInvoicePdfReturnsInlinePdf() throws Exception {
            setPrincipal();
            byte[] pdf = "invoice".getBytes();
            when(pdfService.invoicePdf(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(pdf);

            mockMvc.perform(get("/invoices/77/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(header().string("Content-Disposition", "inline; filename=invoice-77.pdf"))
                    .andExpect(content().bytes(pdf));
        }

        @Test
        void getInvoiceDownloadPdfReturnsAttachment() throws Exception {
            setPrincipal();
            byte[] pdf = "invoice".getBytes();
            when(pdfService.invoicePdf(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(pdf);

            mockMvc.perform(get("/invoices/77/pdf-download"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=invoice-77.pdf"))
                    .andExpect(content().bytes(pdf));
        }

        @Test
        void listPdfsReturnsOk() throws Exception {
            setPrincipal();
            List<InvoicePdfDto> pdfs = List.of(
                    new InvoicePdfDto(11L, "INV_77.pdf", LocalDateTime.of(2025, 1, 15, 10, 0))
            );
            when(invoicePdfService.listVersions(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(pdfs);

            mockMvc.perform(get("/invoices/77/pdfs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(11L))
                    .andExpect(jsonPath("$[0].fileName").value("INV_77.pdf"))
                    .andExpect(jsonPath("$[0].createdAt").value("2025-01-15T10:00:00"));
        }

        @Test
        void downloadPdfReturnsResource() throws Exception {
            setPrincipal();
            byte[] pdf = "pdf".getBytes();
            InvoicePdfDownload download = new InvoicePdfDownload(
                    "INV_77.pdf",
                    new ByteArrayResource(pdf)
            );
            when(invoicePdfService.download(eq(77L), eq(11L), any(AuthPrincipal.class)))
                    .thenReturn(download);

            mockMvc.perform(get("/invoices/77/pdfs/11"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"INV_77.pdf\""))
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(content().bytes(pdf));
        }
    }

    @Nested
    class updateInvoiceTests {
        @Test
        void updateInvoiceReturnsOkWhenValid() throws Exception {
            setPrincipal();
            UpdateInvoiceRequest req = new UpdateInvoiceRequest("New", null, null, null, null);
            when(invoiceService.updateInvoice(eq(77L), any(UpdateInvoiceRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildInvoice());

            mockMvc.perform(patch("/invoices/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L));
        }

        @Test
        void updateInvoiceReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            String longTitle = "x".repeat(201);
            UpdateInvoiceRequest req = new UpdateInvoiceRequest(longTitle, null, null, null, null);

            mockMvc.perform(patch("/invoices/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateInvoiceReturnsServerErrorOnFailure() throws Exception {
            setPrincipal();
            UpdateInvoiceRequest req = new UpdateInvoiceRequest("New", null, null, null, null);
            when(invoiceService.updateInvoice(eq(77L), any(UpdateInvoiceRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(patch("/invoices/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class issueInvoiceTests {
        @Test
        void issueInvoiceReturnsOkWhenValid() throws Exception {
            setPrincipal();
            InvoicePdfArchive archive = buildInvoicePdfArchive();
            when(invoiceService.issueInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(archive);

            mockMvc.perform(post("/invoices/77/issue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.saveId").value(55L))
                    .andExpect(jsonPath("$.invoiceId").value(77L))
                    .andExpect(jsonPath("$.createdAt").value("2025-01-10T09:30:00"));
        }

        @Test
        void issueInvoiceReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(invoiceService.issueInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters"));

            mockMvc.perform(post("/invoices/77/issue"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void issueInvoiceReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            when(invoiceService.issueInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid"));

            mockMvc.perform(post("/invoices/77/issue"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class payInvoiceTests {
        @Test
        void payInvoiceReturnsNoContentWhenOk() throws Exception {
            setPrincipal();
            when(invoiceService.payInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/invoices/77/pay"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void payInvoiceReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(invoiceService.payInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(false);

            mockMvc.perform(post("/invoices/77/pay"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void payInvoiceReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            when(invoiceService.payInvoice(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid"));

            mockMvc.perform(post("/invoices/77/pay"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class overdueInvoiceTests {
        @Test
        void overdueInvoiceReturnsNoContentWhenOk() throws Exception {
            setPrincipal();
            when(invoiceService.markInvoiceOverdue(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/invoices/77/overdue"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void overdueInvoiceReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(invoiceService.markInvoiceOverdue(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(false);

            mockMvc.perform(post("/invoices/77/overdue"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void overdueInvoiceReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            when(invoiceService.markInvoiceOverdue(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid"));

            mockMvc.perform(post("/invoices/77/overdue"))
                    .andExpect(status().isInternalServerError());
        }
    }

    private Invoice buildInvoice() {
        Company company = new Company();
        Customer customer = new Customer(company);
        customer.setDisplayName("Acme Spa");
        Invoice invoice = new Invoice(company, customer);
        invoice.setInvoiceYear(2025);
        invoice.setInvoiceNumber(5);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTitle("Title");
        invoice.setIssueDate(LocalDate.of(2025, 1, 10));
        ReflectionTestUtils.setField(invoice, "id", 77L);
        return invoice;
    }

    private InvoicePdfArchive buildInvoicePdfArchive() {
        Invoice invoice = buildInvoice();
        InvoicePdfArchive archive = new InvoicePdfArchive(
                invoice,
                "companies/10/invoices/77",
                "INV_77_20250110_093000_abcdef.pdf"
        );
        ReflectionTestUtils.setField(archive, "id", 55L);
        ReflectionTestUtils.setField(archive, "createdAt", LocalDateTime.of(2025, 1, 10, 9, 30, 0));
        return archive;
    }
}
