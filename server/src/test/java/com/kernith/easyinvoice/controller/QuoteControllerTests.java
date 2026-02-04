package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.quote.CreateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quote.UpdateQuoteRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.QuoteService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        QuoteControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class QuoteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuoteService quoteService;

    @MockitoBean
    private com.kernith.easyinvoice.service.InvoiceService invoiceService;

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }

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
    class createQuoteTests {
        @Test
        void createQuoteReturnsOkWhenValid() throws Exception {
            setPrincipal();
            CreateQuoteRequest req = new CreateQuoteRequest(
                    100L,
                    "Title",
                    "Notes",
                    null,
                    null,
                    "EUR",
                    List.of(new CreateQuoteItemRequest(
                            1,
                            "Item",
                            null,
                            BigDecimal.ONE,
                            null,
                            BigDecimal.TEN,
                            BigDecimal.ZERO,
                            null,
                            null
                    ))
            );

            Quote quote = buildQuote();
            when(quoteService.createQuote(any(CreateQuoteRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(quote);

            mockMvc.perform(post("/quotes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L))
                    .andExpect(jsonPath("$.quoteNumber").value(5))
                    .andExpect(jsonPath("$.quoteYear").value(2025))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.customerDisplayName").value("Acme Spa"));
        }
    }

    @Nested
    class listQuotesTests {
        @Test
        void listQuotesReturnsPageWhenNotEmpty() throws Exception {
            setPrincipal();
            Quote quote = buildQuote();
            Page<Quote> page = new PageImpl<>(List.of(quote));
            when(quoteService.listQuotes(any(AuthPrincipal.class), eq(0), eq(20), eq("issueDate,desc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/quotes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(77L))
                    .andExpect(jsonPath("$.content[0].quoteNumber").value(5));
        }

        @Test
        void listQuotesReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            Page<Quote> page = Page.empty();
            when(quoteService.listQuotes(any(AuthPrincipal.class), eq(0), eq(20), eq("issueDate,desc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/quotes"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class getQuoteTests {
        @Test
        void getQuoteReturnsQuoteWhenFound() throws Exception {
            setPrincipal();
            when(quoteService.getQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(buildQuote()));

            mockMvc.perform(get("/quotes/77"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L))
                    .andExpect(jsonPath("$.quoteNumber").value(5));
        }

        @Test
        void getQuoteReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(quoteService.getQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/quotes/77"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class updateQuoteTests {
        @Test
        void updateQuoteReturnsOkWhenValid() throws Exception {
            setPrincipal();
            UpdateQuoteRequest req = new UpdateQuoteRequest("New", null, null, null, null);
            when(quoteService.updateQuote(eq(77L), any(UpdateQuoteRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildQuote());

            mockMvc.perform(patch("/quotes/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(77L));
        }
    }

    @Nested
    class statusTransitionTests {
        @Test
        void archiveQuoteReturnsNoContentWhenOk() throws Exception {
            setPrincipal();
            when(quoteService.archiveQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/quotes/77/archive"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void sendQuoteReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(quoteService.sendQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(false);

            mockMvc.perform(post("/quotes/77/send"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void acceptRejectReturnNoContentWhenOk() throws Exception {
            setPrincipal();
            when(quoteService.acceptQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(true);
            when(quoteService.rejectQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/quotes/77/accept"))
                    .andExpect(status().isNoContent());
            mockMvc.perform(post("/quotes/77/reject"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void convertQuoteReturnsOkWhenAccepted() throws Exception {
            setPrincipal();
            when(invoiceService.createInvoiceFromQuote(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(buildInvoice());

            mockMvc.perform(post("/quotes/77/convert"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(88L))
                    .andExpect(jsonPath("$.invoiceNumber").value(10));
        }
    }

    private Quote buildQuote() {
        Company company = new Company();
        Customer customer = new Customer(company);
        customer.setDisplayName("Acme Spa");
        Quote quote = new Quote(company, customer);
        quote.setQuoteYear(2025);
        quote.setQuoteNumber(5);
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setTitle("Title");
        quote.setIssueDate(LocalDate.of(2025, 1, 10));
        org.springframework.test.util.ReflectionTestUtils.setField(quote, "id", 77L);
        return quote;
    }

    private com.kernith.easyinvoice.data.model.Invoice buildInvoice() {
        Company company = new Company();
        Customer customer = new Customer(company);
        customer.setDisplayName("Acme Spa");
        com.kernith.easyinvoice.data.model.Invoice invoice =
                new com.kernith.easyinvoice.data.model.Invoice(company, customer);
        invoice.setInvoiceYear(2025);
        invoice.setInvoiceNumber(10);
        invoice.setStatus(com.kernith.easyinvoice.data.model.InvoiceStatus.DRAFT);
        invoice.setTitle("Invoice");
        invoice.setIssueDate(LocalDate.of(2025, 2, 1));
        org.springframework.test.util.ReflectionTestUtils.setField(invoice, "id", 88L);
        return invoice;
    }
}
